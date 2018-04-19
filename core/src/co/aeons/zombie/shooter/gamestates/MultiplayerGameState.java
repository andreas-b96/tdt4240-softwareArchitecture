package co.aeons.zombie.shooter.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

import co.aeons.zombie.shooter.ZombieShooter;
import co.aeons.zombie.shooter.entities.SecondPlayer;
import co.aeons.zombie.shooter.managers.GameStateManager;
import co.aeons.zombie.shooter.utils.MultiplayerMessage;
import co.aeons.zombie.shooter.utils.enums.MultiplayerState;

import static co.aeons.zombie.shooter.ZombieShooter.cam;
import static co.aeons.zombie.shooter.utils.enums.MultiplayerState.STARTMULTIPLAYER;

public class MultiplayerGameState extends PlayState {

    //Initialize SpriteBatch used to display info about multiplayer state
    private SpriteBatch sb;
    // The second (online) player
    public static SecondPlayer secondPlayer;

    //Max time before the game will start
    private final float MAX_TIME_TO_START_GAME = 5f;
    // Max time to leave the game before it'll start
    private final float MAX_TIME_TO_LEFT_GAME = 1f;

    //These variables are only used if necessary if we need to send the same operation
    //more than once since we're sending over UDP

    // Numer of times to retry sending the same operation
    private final int TIMES_TO_SEND_SAME_OPERATION = 5;

    // Number of times we have sent the operation
    //TODO: change variable name to fit with our game
    private int times_sended_receive_damage_operation;

    //Information message to show to the user
    private BitmapFont infoMessage;
    private GlyphLayout layout;


    //Times start -> finish
    private float timeToStartGame;
    private float timeToLeftGame;

    // FirstPlayer powerups
    //TODO: change these to fit with our powerups
    /*
    public static BurstPowerUp playerBurstPowerUp;
    private static RegLifePowerUp playerRegLifePowerUp;
    private static ShieldPowerUp playerShieldPowerUp;

    //SecondPlayer powerups
    public static BurstPowerUp rivalBurstPowerUp;
    private static RegLifePowerUp rivalRegLifePowerUp;
    private static ShieldPowerUp rivalShieldPoweUp;
    */

    // To know if the FirstPlayer left the game
    private boolean abandonFirstPlayer;

    // To know if the SecondPlayer left the game
    private boolean abandonSecondPlayer;

    //Game input and output messages
    private MultiplayerMessage outcomeMessage;
    private MultiplayerMessage incomeMessage;

    //Check if the player wants to leave the room or not
    private boolean leaveRoom;

    public MultiplayerGameState(GameStateManager gsm, String option) {
        super(gsm);

        sb = new SpriteBatch();

        outcomeMessage = new MultiplayerMessage();
        incomeMessage = new MultiplayerMessage();

        //state = GameState.READY;

        leaveRoom = false;

        times_sended_receive_damage_operation = 0;

        //Show infomessage
        infoMessage = new BitmapFont();
        layout = new GlyphLayout();

        timeToStartGame = MAX_TIME_TO_START_GAME;
        timeToLeftGame = MAX_TIME_TO_LEFT_GAME;

        abandonFirstPlayer = false;
        abandonSecondPlayer = false;

        //TODO: Initialize powerups

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);

        //Switch statement to choose which mode to display
        switch (option) {
            case "QUICK":
                ZombieShooter.googleServices.startQuickGame();
                break;
            case "INVITE":
                ZombieShooter.googleServices.invitePlayer();
                break;
            default:
                ZombieShooter.googleServices.seeMyInvitations();
                break;
        }
    }

    @Override
    public void init() {
        //TODO: Move this to improve multiplayer?
        //super.init();
        // Create second player
        secondPlayer = new SecondPlayer(super.bullets);


    }

    @Override
    public void update(float dt) {

        if (!ZombieShooter.googleServices.getMultiplayerState().equals(STARTMULTIPLAYER)) {
            String message = "Connecting to server";
            this.sb.begin();
            infoMessage.getData().setScale(2, 2);
            layout.setText(infoMessage, message);
            float width = layout.width;
            infoMessage.draw(this.sb, message, (cam.viewportWidth - width) / 2, cam.viewportHeight - 25);
            this.sb.end();
        }
        updateReady(dt);

    }

    public void updateReady(float dt) {
        switch (ZombieShooter.googleServices.getMultiplayerState()) {
            case STARTMULTIPLAYER:

                if (timeToStartGame > 0) {
                    String message = "Game starting in" + (int) timeToStartGame;

                    //Gives us a list of player ids, use one to designate host
                    ArrayList<String> a = ZombieShooter.googleServices.getPlayerIds();

                    //Inform player how much time is left until we can start the game
                    this.sb.begin();
                    infoMessage.getData().setScale(2, 2);
                    layout.setText(infoMessage, message);
                    float width = layout.width;
                    infoMessage.draw(this.sb, message, (cam.viewportWidth - width) / 2, cam.viewportHeight - 25);
                    this.sb.end();
                    timeToStartGame -= dt;
                } else {
                    // code here to handle game start
                    timeToStartGame = 0;

                    //Here we start the game
                    updateStart(dt);
                    //state = GameState.START;
                }
                break;
            case CANCEL:
                gsm.setState(GameStateManager.MENU);
                break;
        }

    }

    public void updateStart(float dt) {
        updateIncomeMessage(dt);
        updateOutComeMessage(dt);
        super.update(dt);


        /*
        if (timeToLeftGame > 0) {
            timeToLeftGame -= dt;
        } else {
            if (Gdx.input.justTouched()) {
                ZombieShooter.googleServices.leaveRoom();
                gsm.setState(GameStateManager.MENU);
            }
        }
        */


        //Act here
    }

    public void updateIncomeMessage(float dt) {
        //Fetch the input message
        incomeMessage = ZombieShooter.googleServices.receiveGameMessage();
        //Check if opponent has requested to leave the room
        if (incomeMessage.checkOperation(incomeMessage.MASK_LEAVE)) {
            abandonSecondPlayer = true;
            //TODO: exit game or something here?
        }
        // Petición recibida de disparo
        if (incomeMessage.checkOperation(incomeMessage.MASK_SHOOT))
            System.out.println("Shoot");
        //rivalShip.shoot();
        // Petición recibida de powerUp Burst usado
        if (incomeMessage.checkOperation(incomeMessage.MASK_BURST))
            System.out.println("burst");
        //rivalBurstPowerUp.setTouched();
        // Petición recibida de powerUp Regeneración de Vida usado
        if (incomeMessage.checkOperation(incomeMessage.MASK_REG_LIFE))
            System.out.println("life powerup");
        //rivalRegLifePowerUp.setTouched();
        // Petición recibida de powerUp Escudo
        if (incomeMessage.checkOperation(incomeMessage.MASK_SHIELD))
            System.out.println("Shield powerup");
        //rivalShieldPoweUp.setTouched();
        // Petición recibida de recepción de daño
        if (incomeMessage.checkOperation(incomeMessage.MASK_HAS_RECEIVE_DAMAGE)) {
            System.out.println("Damage received");
            //rivalShip.receiveDamage();
        }
        /*
        TODO:Fix win state
        if(rivalShip.isCompletelyDefeated()){
            state = GameState.WIN;
        }
        */
        //Update logic of the rival
        //rivalShip.update(delta,incomeMessage.getPositionY());
        secondPlayer.setPosition(secondPlayer.getx(), incomeMessage.getPositionY());

        // Reset for next update
        incomeMessage.resetOperations();


    }

    public void updateOutComeMessage(float dt) {
        //Update outcome message
        Vector2 tmpVec =  new Vector2();
        stage.getViewport().unproject(tmpVec.set(Gdx.input.getX(),Gdx.input.getY()));
        outcomeMessage.setPositionY(tmpVec.y);

        //Finally we send the message

        ZombieShooter.googleServices.sendGameMessage(outcomeMessage.getForSendMessage());

        //We reset the operations so as not to interfere in the next interaction
        outcomeMessage.resetOperations();


    }


    @Override
    public void draw() {
        sb.setProjectionMatrix(cam.combined);
        //Draw other player
        //TODO: Move second player to super spritebatch?
        secondPlayer.draw(sb);
    }


    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.BACK) {
            leaveRoom = true;
        }
        return false;
    }

}
