//@Charlie Dean
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Random;


public class Server implements Auction{

    public static String name;
    public Server(String stubName){ 
        super(); 
        Server.name = stubName;
    }


    AuctionItem Messi =  AuctionItem(1,"Messi","Footballer",120);
    AuctionItem Noob =  AuctionItem(2,"Ronaldo","Footballer",150);
    AuctionItem NotFound = AuctionItem(3,"NotFound","N/A",0);
    int ItemIDs[] = new int[300];
    //Stores values to iterate through
    int j = -1;
    int s = 0;
    int iteration = 0;
    //Stores arrays to store User Information
    AuctionItem Items[] = new AuctionItem[300];
    int WinningIDs[] = new int[300];
    int WinningItems[] = new int[300];
    int WinningPrice[] = new int[300];
    int Reserveprices[] = new int[300];
    int CurrentUserIds[] = new int[300];
    String CurrentEmails[] = new String[500];
    PublicKey keylistPublicKeys[] = new PublicKey[500];
    int AuctionUserIds[] = new int[300];

    public synchronized AuctionItem AuctionItem(int ID, String n, String desc, int high){
        AuctionItem item = new AuctionItem();
        item.itemID = ID;
        item.name = n;
        item.description = desc;
        item.highest = high;

        return item;
    }

    public synchronized AuctionCloseInfo AuctionCloseInfo(String WEmail,int Wprice){
        AuctionCloseInfo Person = new AuctionCloseInfo();
        Person.winningEmail = WEmail;
        Person.winningPrice = Wprice;
        return Person;
    }


    public AuctionItem getSpec(int n){
        try {
            for (int i = 0; i<Items.length;i++){
                if (Items[i].itemID == n){
                    return Items[i];
                }
            }
            return NotFound;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return Messi;
    }
    
    //This Function Creates a USERID, public and private key for the User after they have entered their email.
    @Override
    public synchronized NewUserInfo newUser(String email) throws RemoteException  {
        try {
            String Useremail = email;
            //Creates a new instance of the class newUserInfo
            NewUserInfo UserPerson = new NewUserInfo();
            Random rand = new Random();
            //Creates a Random number from 0-100.
            int randomUserID = rand.nextInt(100);
            //Runs a FOR loop to check if the randomID is already connected to a user account.
            for (int z = 0;z<CurrentUserIds.length;z++){
                try {
                    if (CurrentUserIds[z] == randomUserID){
                        randomUserID = rand.nextInt(100);
                        z=0;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            //Sets array values to the previously created values
            int UserID = randomUserID;
            CurrentUserIds[iteration] = UserID;
            CurrentEmails[iteration] = Useremail;
            UserPerson.userID = UserID;


            //Creates the public and private for the user.
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
    
            byte[] privateKeyBytes = privateKey.getEncoded();
            byte[] publicKeyBytes = publicKey.getEncoded();
    
            UserPerson.privateKey = privateKeyBytes;
            UserPerson.publicKey = publicKeyBytes;
            keylistPublicKeys[iteration] = publicKey;
    
    
            iteration = iteration+1;
            return UserPerson;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    //This Function creates a new Auction for the user, ands lists their item.
    //This returns an ITEMID for the auction sale item inputted by the user.
    @Override
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        j++;
        AuctionSaleItem UserItem = item;
        Random random = new Random();
        //
        int randomItemID = random.nextInt(100);
        //Runs a FOR loop to check if it randomitemID created is already in the ItemsID list.
        for (int x = 0; x<ItemIDs.length;x++){
            if(ItemIDs[x] == randomItemID){
                newAuction(userID, UserItem);
            }
        }
        //Places the randomID in the ItemIDs
        ItemIDs[j] = randomItemID;
        Reserveprices[j] = UserItem.reservePrice;
        AuctionUserIds[j] = userID;
        //Converts the AuctionSaleItem to a new AuctionItem then places it in the Item array.
        AuctionItem Converted = AuctionItem(randomItemID, UserItem.name, UserItem.description, 0);
        Items[j] = Converted;
        return ItemIDs[j];
    }

    //This function returns the Items Array.
    @Override
    public AuctionItem[] listItems() throws RemoteException {
        return Items;
    }

    //This Function closes a certain auction for an inputted item and returns who won the item.
    @Override
    public AuctionCloseInfo closeAuction(int userID, int itemID) throws RemoteException {
        //Variables to store the winningprice and winningID
        int a = 0;
        int g = 0;
        String Email = "";
        //Runs a Forloop going through every item in the list
        for (int q = 0; q<Items.length; q++ ){
            try {
                if (Items[q].itemID == itemID){
                    //Checks if the items highest is below the reserve price if so it returns "None"
                    if (userID == AuctionUserIds[q]){
                        if (Items[q].highest < Reserveprices[q]){
                            for (int o = 0; o<Items.length; o++ ){
                                try {
                                    if (Items[o].itemID == itemID){
                                        Items[o] = null;
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            }
                            AuctionCloseInfo None = AuctionCloseInfo("None", 0);
                            return None;
                        }
                        //Checks that the reserve price is lower then the highest.
                        if (Items[q].highest >= Reserveprices[q]){
                                for (int v = 0; v<WinningItems.length; v++ ){
                                    try {
                                        //IF this is true then it makes the variables a and b equal the winning price and winning id.
                                        if (WinningItems[v] == itemID){
                                            if (WinningPrice[v] > a){
                                                a = WinningPrice[v];
                                                g = WinningIDs[v];
                                            }
                                        }
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                                //Removes the item selected from the Item array
                                for (int o = 0; o<Items.length; o++ ){
                                    try {
                                        if (Items[o].itemID == itemID){
                                            Items[o] = null;
                                        }
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                                //Runs a for loop through all users IDS
                                for(int f = 0;f<CurrentUserIds.length;f++){
                                    try {
                                        //Checks the index where the winningID equals the currentIDs
                                        if (CurrentUserIds[f] == g){
                                            //Finds the email of the winningID
                                            Email = CurrentEmails[f];
                                            //Creates a new instance of AUctionCloseInfo and returns it as winner.
                                            AuctionCloseInfo Winner = AuctionCloseInfo(Email, a);
                                            return Winner;
                                        }
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return null;

    }


    //THis function allows the user to bid on items based on their ID, itemID and price.
    @Override
    public synchronized boolean bid(int userID, int itemID, int price) throws RemoteException { 
        //Runs a for loop going through the items list
        for (int k =0; k<Items.length;k++){
            try {
                //checks if the itemsID in the items list is equal to the entered user itemID
                if (Items[k].itemID == itemID){
                    //If true then it checks that the price the User entered is more then the highest current price on the item.
                    if (Items[k].highest < price){
                        //IF true then it sets the variables to accompny the users bid
                        Items[k].highest = price;
                        WinningIDs[s] = userID;
                        WinningItems[s] = itemID;
                        WinningPrice[s] = price;
                        s++;
                        return true;
                    }
                    //IF the price is less then the highest it returns false
                    if (Items[k].highest >= price){
                        return false;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return false;
    }

    //This function creates the keypair(public and private key) for the Server and writes them into the keys file.
    private static KeyPair getKeyPair() throws NoSuchAlgorithmException, IOException {
        //Ensures the key instance is RSA
        KeyPairGenerator kpg  = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        //Creates the keypair using RSA instance
        KeyPair kp = kpg.generateKeyPair();
        //Sets the public and private key to variables
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();
        //Stores the keys in the key file
        FileOutputStream out = new FileOutputStream("../keys/server_public.key");
        out.write(pub.getEncoded());
        out.close();

        FileOutputStream out2 = new FileOutputStream("../keys/server_private.key");
        out2.write(pvt.getEncoded());
        out2.close();

        return kp;
    }

    //This function creates a challenge containing the signature of the string "auction" for the client
    @Override
    public byte[] challenge(int userID)throws RemoteException{
        try {
            //Reads the bytes of the private key
            byte[] keyBytes = Files.readAllBytes(Paths.get("../keys/server_private.key"));
            //Encodes the keybytes
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            //Ensures the signature uses the instance "SHA256withRSA"
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(kf.generatePrivate(spec));
            //Creates the string "auction" and updates the private signature with a UTF-8 converted version of the message.
            String message = "auction";
            privateSignature.update(message.getBytes("UTF-8"));
            //creates a byte[] of the signed signature
            byte[] signature = privateSignature.sign();
            //returns the signature
            return signature;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    //This function authenticates a clients challenge
    //The clients challenge would be a string of their email
    @Override
    public boolean authenticate(int userID, byte[] signature) throws RemoteException {
        try {
            //Creates an instance
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            //Sets the email and key to null
            String CurrentEmail = "";
            PublicKey CurrentKey = keylistPublicKeys[0];
            //runs a for loop going through all userIDs
          for (int h=0;h<CurrentUserIds.length;h++){
              try {
                  //finds the userID index found in the parameters
                  if (CurrentUserIds[h] == userID){
                    //Sets the key and email to equal the users known key and email
                      CurrentKey = keylistPublicKeys[h];
                      CurrentEmail = CurrentEmails[h];
                      //Makes a new message contains their email in bytes
                      byte[] message = CurrentEmail.getBytes("UTF-8");
                      //Verifys the signature with the users public key
                      publicSignature.initVerify(CurrentKey);
                      //Updates the signature with the encoded message
                      publicSignature.update(message);
                      //returns if the verification of signature is true or false
                      return publicSignature.verify(signature);
                  }
              } catch (Exception e) {
                  // TODO: handle exception
              }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }









    public static void main(String[] args){
        try {
            Server s = new Server(name);
            Auction stub = (Auction) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            //Runs the getKeyPair function to create the keys
            getKeyPair();
            System.out.println("Keys created");
            System.out.println("Server ready");
           } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        return Integer.parseInt(name);
    }




}
