//@Charlie Dean
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Client{

    //Stores Your Private Key and Your Email as a public static to be accessed throughout your Client.
    public static byte[] YourKey;
    public static String youremail;

    public static AuctionSaleItem AuctionSaleItem(String n, String desc, int rP){
      AuctionSaleItem item = new AuctionSaleItem();
      item.name = n;
      item.description = desc;
      item.reservePrice = rP;

      return item;
  }

    //Runs the Start of the Auction making the person either Create a User or Enter their UserID
    //Returns their option.
    public static String AuctionStart(){
      String n;
      System.out.println("Auction System");
      Scanner sc1= new Scanner(System.in);  
      System.out.println("1: Create a new User \n2: Enter UserID");
      n = sc1.nextLine();
      return n;

     }
     //Can Run the Auction Optionss for the User allowing them to select between 4 options.
     //Returns the option they chose.
     public static String AuctionOptions(){
      String option;
      System.out.println("Auction System");
      Scanner sc1= new Scanner(System.in);  
      System.out.println("1: Start Auction \n2: Make Bid\n3: List Items\n4: Close Auction");
      option = sc1.nextLine();
      return option;
     }

     //Creates a challenge for the server containing a signature of the users email string encoded with the users private key.
     //returns the signature
     public static byte[] challenge(int userID) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException, IOException {
      //Generates your private key from the YourKey variable
      PrivateKey privateKey =  KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(YourKey));
      //Converts youremail to bytes and stores it in data
      byte[] data = youremail.getBytes("UTF-8");
      Signature privateSignature = Signature.getInstance("SHA256withRSA");
      //initsigns the data with your privatekey
      privateSignature.initSign(privateKey);
      //updates the signature with the encoded data
      privateSignature.update(data);
      //returns the signed signature
      return privateSignature.sign();
      }


      //Runs the client side authentication which uses the server challenge signature to verify if the string "auction" is equal to the decoded signature
      //This string is decoded with the servers public key
      //returns if the signature is correct.
      public static boolean authenticate(int userID, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException, IOException {
        //Reads the bytes of the servers public key
        byte[] keyBytes = Files.readAllBytes(Paths.get("../keys/server_public.key"));
        //creates a string of the message "auction"
        String message = "auction";
        //converts the message to bytes in form UTF-8
        byte[] converted = message.getBytes("UTF-8");
        //encodes the keybytes of the public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");  
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        //initVerifys the signature with the public key
        publicSignature.initVerify(kf.generatePublic(spec));
        //Updates the signature to include the converted message bits
        publicSignature.update(converted);
        //creates a boolean to verify if the signature matches the message "auction"
        boolean iscorrect = publicSignature.verify(signature);
        //returns the boolean
        return iscorrect;
    }


     public static void main(String[] args) {

         try {
               
          
               String name = "FrontEnd";
               Registry registry = LocateRegistry.getRegistry("localhost");
               Auction server = (Auction) registry.lookup(name);

               //Creates 3 Variables that will be changed and need to be stored outside the Auction loop.
               //User ID
               //Your AuctionIDs stores all IDS of every active Auction currently running.
               int UserID = 0;
               int[] YourAuctionIDs = new int[100];
               int z = 0;

               while (true){

                //Runs Auction Start to recieve the first users input
                //Can Output eith 1 or 2.
                String Options = AuctionStart();

                //If Option 1 is selected then It asks the user to enter their email and returns them a UserID
                //This saves their email and private key to their own terminal/instance of client.
                if (Options.equals("1")){
                  boolean Correct = true;
                  while (Correct){
                    Scanner sc1= new Scanner(System.in); 
                    System.out.println("Please enter your email to create a new User");
                    String str= sc1.nextLine();
                    //Checks that the email is valid and contains an @ symbol
                    if (str.contains("@")){
                      System.out.print("You have entered: "+str + " ");  
                      NewUserInfo User = server.newUser(str);
                      System.out.println("Ur User ID is: " + User.userID);  
                      youremail = str;
                      YourKey = User.privateKey;
                      Correct = false;
                    }
                    else{
                      System.out.println("MUST CONTAIN AN VALID EMAIL WITH AN @");
                    } 
                  }             
                }
                //If Option 2 is selected then it asks the user to enter their created ID.
                else if (Options.equals("2")){
                  String str2 = "";
                  int Verified = 0;
                  try {
                    Scanner sc1= new Scanner(System.in); 
                    System.out.println("Please enter your UserID");
                    str2= sc1.nextLine();               
                    System.out.println("You have entered: "+str2 + " "); 
                    UserID = Integer.parseInt(str2);
                  } catch (Exception e) {
                    Verified = 0;
                  }

                  //Verification System
                  //This first creates a challange by the server to the client.
                  byte[] challenge = server.challenge(UserID);
                  //The client must then autheticate the servers challenge.
                  boolean authenticater =authenticate(UserID, challenge);
                  try {
                    //Checks if the authenticate from the client is true
                    if (authenticater){
                      //Creates a challenge for the server to verify the server
                      byte[] challenge2 = challenge(UserID);
                      //Runs the servers autheticate function to ensure the server is verified correctly.
                      boolean authenticater2 =server.authenticate(UserID, challenge2);
                      //Checks if the server is correctly authenticated
                      if(authenticater2){
                        //If true then verified becomes true
                        Verified =1;
                        System.out.println("Verified");
                      }
                    }
                  } catch (Exception e) {
                    Verified = 0;
                  }
                  //IF the users ID is correctly verified then the connection from client to server begins.
                  if (Verified == 1){
                    while (true){
                      //Runs AuctionOptions to allow the user what action they wish to peform.
                      String AuctionOptions = AuctionOptions();
                      //Action 1 is to create a new Auction
                      if (AuctionOptions.equals("1")){
                        boolean input = false;
                        try {
                        while (input == false){
                          Scanner sc2= new Scanner(System.in); 
                          System.out.println("Please enter Items Name");
                          String ItemName = sc2.nextLine();   
                          System.out.println("Please enter Items Desc");
                          String ItemDesc = sc2.nextLine(); 
                          String ItemRP = "0";
                          System.out.println("Please enter Items reserve price");
                          ItemRP = sc2.nextLine(); 
                          input = true; 
                          AuctionSaleItem Item = AuctionSaleItem(ItemName,ItemDesc,Integer.parseInt(ItemRP));     
                          int YourID = server.newAuction(Integer.parseInt(str2), Item);
                          //Stores all the current clients Auction IDs in an array.
                          YourAuctionIDs[z] = YourID;
                          z = z+1;
                          System.out.println("Your Auction is open");
                        } 
                        //Allows the user to create a auction item and run the servers newAuction function to list it.
                        } catch (Exception e) {
                          System.out.println("MUst enter the correct input values");
                          e.printStackTrace();
                          input = true;
                        }
                      }
                      //If action 2 is picked it allows the User to bid on current items.
                      if (AuctionOptions.equals("2")){
                        AuctionItem[] ListOfItems = server.listItems();
                        boolean loop = true;
                        int None = 0;
                        //Runs a while and for loop outputting all the currently active auction items.
                        while (loop == true){
                          for (int y =0; y<ListOfItems.length;y++){
                            try {
                              System.out.println("ItemID: " + ListOfItems[y].itemID + " Name: " + ListOfItems[y].name + " Description: " + ListOfItems[y].description + " HighestBid: " + ListOfItems[y].highest);
                            } catch (Exception e) {
                              loop = false;
                              None++;
                            }
                          }
                        }
                        //If None does not equal 100 that means there are ITEMS currently active to bid on.
                        if (None != 100){
                          boolean correctinput = true;
                          try {
                            while (correctinput){
                          //Creates a scanner to allow user input on the itemID they wish to bid on
                          Scanner sc3= new Scanner(System.in); 
                          System.out.println("Please enter Items ID you would like to bid on");
                          String BiddingID = sc3.nextLine(); 
                          int BiddingError = 0;
                          //Runs a FOR loop to check if the item the user tried to bid on is their own.
                          for (int o = 0; o<YourAuctionIDs.length;o++){
                              if (YourAuctionIDs[o] == Integer.parseInt(BiddingID) & Integer.parseInt(BiddingID) != 0){
                                //If you try to bid on ur own item that creates a Bidding error/makes bidding error true/1
                                System.out.println("Cannot bid on ur own Item");
                                BiddingError = 1;
                                correctinput = false;
                              }
                          }
                          //If BiddingError is equal to 0/false the following code will run.
                          if (BiddingError == 0){
                            //Runs a FOR loop going through all active items again
                            for (int a = 0;a<ListOfItems.length;a++){
                              try {
                                //Checks if an items ID is equal to the item you wish to bid on
                                if (ListOfItems[a].itemID == Integer.parseInt(BiddingID)){
                                  boolean Bidding = true;
                                  System.out.println("Entered Bidding for Item " + BiddingID);
                                  while (Bidding){
                                    //Creates a prompt and scanner to allow the user to enter the amount they would wish to bid on the item.
                                    System.out.println("How much would you like to bid");
                                    String BiddingPrice = sc3.nextLine();   
                                    //Runs the auction servers bid function
                                    boolean Bid = server.bid(Integer.parseInt(str2), Integer.parseInt(BiddingID), Integer.parseInt(BiddingPrice));
                                    //if the Bid boolean comes back false then it informs the user their bid was uinvalid
                                    if (Bid == false){
                                      System.out.println("Invalid Bid, please try again");
                                      Bidding = false;
                                      correctinput = false;
                                    }
                                    //Checks if the BID function returns true, if so then the bid was a success
                                    else if(Bid == true){
                                      System.out.println("Bid Successful");
                                      Bidding = false;
                                      correctinput = false;
                                    }
                                  }
                                  break;
                                }
                              } catch (Exception e) {
                                correctinput = false;
                              }
                            }
                          }
                            }
                          } catch (Exception e) {
                            System.out.println("Must enter the correct input");
                          }
                          } 
                          //IF no items are currently in the auction then it informs the user
                          else if (None == 100){
                            System.out.println("No Items to Currently Bid on");
                          }
                      }
                      //IF action 3 is selected then it simply calls the server.list items function 
                      //runs a FOR loop going through the current items in the Auction server, then prints them to the screen
                      if (AuctionOptions.equals("3")){
                        AuctionItem[] ListOfItems = server.listItems();
                        boolean loop = true;
                        while (loop == true){
                          for (int y =0; y<ListOfItems.length;y++){
                            try {
                              System.out.println("ItemID: " + ListOfItems[y].itemID + " Name: " + ListOfItems[y].name + " Description: " + ListOfItems[y].description + " HighestBid: " + ListOfItems[y].highest);
                            } catch (Exception e) {
                              loop = false;
                            }
                          }
                        }
                      }
                      //IF action 4 is selected then it brings the user to the close auction option.
                      if (AuctionOptions.equals("4")){
                        System.out.println("Current Active Items");
                        AuctionItem[] ItemList = server.listItems();
                        int None = 0;
                        //Runs a FOR loop to run through all the current Active Items
                        for (int l =0; l<ItemList.length;l++){
                          try {
                            System.out.println("ItemID: " + ItemList[l].itemID + " Name: " + ItemList[l].name + " Description: " + ItemList[l].description + " HighestBid: " + ItemList[l].highest);
                          } catch (Exception e) {
                            None++;
                          }
                        }
                        //IF there are items currently listed for auction it proceeds with the next lines of code.
                        if (None != 100){
                          boolean inputted = true;
                          try {
                          while (inputted){
                          //Creates a scanner asking the user the ID of the item they wish to close.
                          Scanner sc3 = new Scanner(System.in); 
                          System.out.println("Enter the ID of the Item you wish to close");
                          String YourChosenID = sc3.nextLine(); 
                          //Runs the servers close Auction function using the usersID and the ID they wish to close.
                          AuctionCloseInfo Winner = server.closeAuction(UserID, Integer.parseInt(YourChosenID));
                          //If the output of winner is null, the ID they entered wasnt theirs to close.
                          if (Winner == null){
                            System.out.println("NOT YOUR ID TO END");
                            inputted = false;
                          }
                          //IF the output of winner was "None" this means the item they closed sold for less then their reserve price
                          else if (Winner.winningEmail.equals("None")){
                            System.out.println("Item was less then reserve price, No Winner"); 
                            inputted = false;               
                          }
                          //IF the output of winner is not equal to null or NONE this measn there was a winner and it print the winning email and price
                          else if (Winner !=null){
                            System.out.println("Winning Email was " + Winner.winningEmail + " Winning Price was " + Winner.winningPrice);
                            inputted = false;
                          }
                          }
                          } catch (Exception e) {
                            System.out.println("Must enter Integer value");
                            e.printStackTrace();
                          }
                        }
                        //IF none equalled 100 this means there was no Currently active items to bid on.
                        else if (None == 100){
                          System.out.println("No Current Active Items");
                        }
                        }
                      }
                  }
                  //IF the Verification failed and equalled false/0 it informs the user of this and resets
                  else if (Verified == 0){
                    System.out.println("Sorry Couldnt Verify");
                  }
                  }
                }
              }
             catch (Exception e) {
             e.printStackTrace();
             System.err.println("Connection Lost : Reconnecting");
             }
            }
          }