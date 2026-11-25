package com.account;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Core model (in memory until the database step)
        Bank bank = new Bank("Botswana Digital Bank", "Main Branch");
        //bank.seedDemoData();

        // Controllers sit between the views and the model
        LoginController loginController = new LoginController(bank);

        RegistrationController registrationController =
                new RegistrationController(bank);

        AccountController accountController =
                new AccountController(bank);

        AccountTransactionController transactionController =
                new AccountTransactionController(accountController);

        // One Scene for the whole application;
        // the Navigator swaps its root
        Scene scene = new Scene(new StackPane(), 1100, 740);

        
        // LOAD EXTERNAL CSS STYLESHEET
        

        try {

            java.nio.file.Path cssPath =
                    java.nio.file.Paths.get(
                            "src",
                            "resources",
                            "style.css"
                    ).toAbsolutePath();

            if (java.nio.file.Files.exists(cssPath)) {

                String cssURL = cssPath.toUri().toString();

                scene.getStylesheets().add(cssURL);

                System.out.println("SUCCESS: CSS LOADED!");
                System.out.println("CSS LOCATION: " + cssURL);

            } else {

                System.out.println("ERROR: CSS FILE NOT FOUND!");
                System.out.println("Expected location: " + cssPath);
            }

        } catch (Exception e) {

            System.out.println("CSS LOADING ERROR!");
            e.printStackTrace();
        }

        
        // APPLICATION NAVIGATION
        

        Navigator navigator = new Navigator(
                stage,
                scene,
                loginController,
                registrationController,
                accountController,
                transactionController
        );

        // Configure stage
        stage.setScene(scene);

        stage.setMinWidth(960);
        stage.setMinHeight(640);

        // Display login screen
        navigator.showLogin();

        stage.show();
    }

    public static void main(String[] args) {

        launch(args);

    }
}
