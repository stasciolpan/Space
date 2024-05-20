package org.example.space;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class SpaceInvaders extends Application {

   private static final Random RAND = new Random();
   private static final int WIDTH = 800;
   private static final int HEIGH = 600;
   private static final int PLAYER_SIZE = 60;

   static final Image PLAYER_IMAGE= new Image("file:player.png");
   static final Image EXPLOSION_IMAGE= new Image("file:explosion.png");

   static final int EXPLOSION_W = 128;
   static final int EXPLOSION_ROW = 3;
   static final int EXPLOSION_COL = 3;
   static final int EXPLOSION_H = 128;
   static final int EXPLOSION_STEPS = 15;

   static final Image BOMBASTIC_IMG[] ={
   new Image("file:1.png"),
   new Image("file:2.png"),
   new Image("file:3.png"),
   new Image("file:4.png"),
   new Image("file:5.png"),
   new Image("file:6.png")
    };

final int MAX_BOMBS = 10, MAX_SHOTS = MAX_BOMBS *2;
boolean gameOver = false;
private GraphicsContext gc;


Rocket player;
List<Shot> shots;
List<Universe> univ;
List<Bomb> Bombs;

private double mouseX;
private int score;

    @Override
    public void start(Stage stage) throws IOException {

        Canvas canvas= new Canvas(WIDTH,HEIGH);
        gc= canvas.getGraphicsContext2D();
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100),e->run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e->mouseX=e.getX());
        canvas.setOnMouseClicked(e->{
            if (shots.size()<MAX_SHOTS) shots.add(player.shoot());
            if (gameOver){
                gameOver= false;
                setup();
            }
        });
        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("Space Invaders");
        stage.show();
    }

    private void setup(){
        univ=new ArrayList<>();
        shots=new ArrayList<>();
        Bombs =new ArrayList<>();
        player =new Rocket(WIDTH/2,HEIGH-PLAYER_SIZE, PLAYER_SIZE,PLAYER_IMAGE);
        score=0;
        IntStream.range(0,MAX_BOMBS).mapToObj(i->this.newBomb()).forEach(Bombs::add);
    }


    private void run(GraphicsContext gc){
        gc.setFill(Color.grayRgb(20));
        gc.fillRect(0,0,WIDTH,HEIGH);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.WHITE);
        gc.fillText("Score: "+score, 60,20);

        if (gameOver){
            gc.setFont(Font.font(30));
            gc.setFill(Color.YELLOW);
            gc.fillText("Game Over \n Scorul tau: "+score+"\n Click pentru a continua.",WIDTH/2,HEIGH/2.5);
        }
        univ.forEach(Universe::draw);

        player.update();
        player.draw();
        player.posX= (int) mouseX;

        Bombs.stream().peek(Rocket::update).peek(Rocket::draw).forEach(e->{
            if (player.colide(e) && !player.exploding){
                player.explode();
            }
        });

        for (int i = shots.size()-1; i>=0; i--){
            Shot shot=shots.get(i);
            if (shot.posY<0 || shot.toRemove){
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for (Bomb bomb :Bombs){
                if (shot.colide(bomb) && !bomb.exploding){
                    score++;
                    bomb.explode();
                    shot.toRemove=true;
                }
            }

        }

        for (int i =Bombs.size()-1; i>=0;i--){
            if (Bombs.get(i).destroyed) {
                Bombs.set(i,newBomb());
            }
        }

        gameOver = player.destroyed;
        if (RAND.nextInt(10)>2){
            univ.add(new Universe());
        }

        for (int i =0; i<univ.size();i++){
            if (univ.get(i).posY>HEIGH){
                univ.remove(i);
            }
        }

    }



    public class Rocket{
        int posX, posY, size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep =0;

        public Rocket(int posX, int posY, int size, Image image){
            this.posX=posX;
            this.posY=posY;
            this.size=size;
            img=image;

        }

        public Shot shoot(){
            return new Shot(posX+size/2 - Shot.size / 2, posY-Shot.size);
        }

        public void update(){
            if(exploding) explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;
        }

        public void draw() {
            if (exploding){
                gc.drawImage(EXPLOSION_IMAGE,explosionStep % EXPLOSION_COL*EXPLOSION_W,
                        (explosionStep/EXPLOSION_ROW)*EXPLOSION_H+1, EXPLOSION_W,EXPLOSION_H,posX,posY,size,size);
            }
            else {
                gc.drawImage(img,posX,posY,size,size);
            }
        }

        public boolean colide(Rocket other){
            int d = distance(this.posX + size/2, this.posY +size / 2,
                    other.posX+other.size / 2, other.posY+other.size/2);
            return d<other.size/2 + this.size/2;
        }

        public void explode(){
            exploding= true;
            explosionStep = -1;
        }
    }

    public class Bomb extends Rocket{
        int SPEED = (score/5)+2;
        public Bomb(int posX, int posY, int size, Image image){
            super(posX,posY,size,image);
        }
        public void update(){
            super.update();
            if(!exploding && !destroyed) posY+=SPEED;
            if (posY>HEIGH) destroyed=true;
        }
    }

    public class Shot{
        public boolean toRemove;
        int posX, posY, speed =10;
        static final int size=6;
        public Shot(int posX,int posY){
            this.posX=posX;
            this.posY=posY;
        }

        public void update(){
            posY-=speed;
        }
        public void draw(){
            gc.setFill(Color.RED);
            if (score >=50 &&score <=70 || score>=120){
                gc.setFill(Color.YELLOWGREEN);
                speed=50;
                gc.fillRect(posX-5, posY-10, size+10, size+30);
            }
            else{
                gc.fillRect(posX, posY, size, size);
            }
        }

        public boolean colide(Rocket rocket){
            int distance = distance(this.posX+size /2 , this.posY+size/2,
                    rocket.posX+ rocket.size/2, rocket.posY+rocket.size/2);
            return distance < rocket.size/2 + size/2;
        }
    }


    public class Universe {
        int posX, posY;
        private int h, w, r, g, b;
        private double opacity;

        public Universe() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5) + 1;
            h = RAND.nextInt(5) + 1;
            r = RAND.nextInt(100) + 150;
            g = RAND.nextInt(100) + 150;
            b = RAND.nextInt(100) + 150;
            opacity = RAND.nextFloat();
            if (opacity < 0) opacity *= -1;
            if (opacity > 0.5) opacity = 0.5;
        }

        public void draw() {
            if (opacity > 0.8) opacity -= 0.01;
            if (opacity < 0.1) opacity += 0.01;
            gc.setFill(Color.rgb(r, g, b, opacity));
            gc.fillOval(posX, posY, w, h);
            posY += 20;
        }
    }
        Bomb newBomb(){
            return new Bomb(50+RAND.nextInt(WIDTH-100),0,PLAYER_SIZE,
                    BOMBASTIC_IMG[RAND.nextInt(BOMBASTIC_IMG.length)]);
        }

        int distance(int x1,int y1, int x2, int y2){
            return (int) Math.sqrt(Math.pow((x1-x2),2)+ Math.pow((y1-y2),2));
        }

    public static void main(String[] args) {
        launch();
    }
}
//public void start(Stage stage) throws IOException {
//    FXMLLoader fxmlLoader = new FXMLLoader(SpaceInvaders.class.getResource("hello-view.fxml"));
//    Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//    stage.setTitle("Hello!");
//    stage.setScene(scene);
//    stage.show();
//}