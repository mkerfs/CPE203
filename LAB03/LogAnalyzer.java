import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class LogAnalyzer
{
      //constants to be used when pulling data out of input
      //leave these here and refer to them to pull out values
   private static final String START_TAG = "START";
   private static final int START_NUM_FIELDS = 3;
   private static final int START_SESSION_ID = 1;
   private static final int START_CUSTOMER_ID = 2;
   private static final String BUY_TAG = "BUY";
   private static final int BUY_NUM_FIELDS = 5;
   private static final int BUY_SESSION_ID = 1;
   private static final int BUY_PRODUCT_ID = 2;
   private static final int BUY_PRICE = 3;
   private static final int BUY_QUANTITY = 4;
   private static final String VIEW_TAG = "VIEW";
   private static final int VIEW_NUM_FIELDS = 4;
   private static final int VIEW_SESSION_ID = 1;
   private static final int VIEW_PRODUCT_ID = 2;
   private static final int VIEW_PRICE = 3;
   private static final String END_TAG = "END";
   private static final int END_NUM_FIELDS = 2;
   private static final int END_SESSION_ID = 1;

      //a good example of what you will need to do next
      //creates a map of sessions to customer ids
   private static void processStartEntry(
      final String[] words,
      final Map<String, List<String>> sessionsFromCustomer) {
      if (words.length != START_NUM_FIELDS) {
         return;
      }

         //check if there already is a list entry in the map
         //for this customer, if not create one
      List<String> sessions = sessionsFromCustomer.get(words[START_CUSTOMER_ID]);
      if (sessions == null) {
         sessions = new LinkedList<>();
         sessionsFromCustomer.put(words[START_CUSTOMER_ID], sessions);

      }

         //now that we know there is a list, add the current session
      sessions.add(words[START_SESSION_ID]);
      // System.out.println(sessions); //Test
   }

      //similar to processStartEntry, should store relevant view
      //data in a map - model on processStartEntry, but store
      //your data to represent a view in the map (not a list of strings)
   private static void processViewEntry(final String[] words
      , final Map<String, List<View>> viewsFromSession) // gonna want a session, product, price (refer to printOutExample)
   {
      if (words.length != VIEW_NUM_FIELDS) {
         return;
      }

      List<View> views = viewsFromSession.get(words[VIEW_SESSION_ID]);
      if (views == null) {
            views = new LinkedList<>();
            viewsFromSession.put(words[VIEW_SESSION_ID], views);
      }
      views.add(new View(words[VIEW_SESSION_ID], words[VIEW_PRODUCT_ID], Integer.parseInt(words[VIEW_PRICE])));
   }

      //similar to processStartEntry, should store relevant purchases
      //data in a map - model on processStartEntry, but store
      //your data to represent a purchase in the map (not a list of strings)
   private static void processBuyEntry(
      final String[] words, final Map<String, List<Buy>> buysFromSession) {
      if (words.length != BUY_NUM_FIELDS){
         return;
      }  
      List<Buy> purchases = buysFromSession.get(words[BUY_SESSION_ID]);
      if (purchases == null) {
            purchases = new LinkedList<>();
            buysFromSession.put(words[BUY_SESSION_ID], purchases);
         }
      
      purchases.add(new Buy(words[BUY_SESSION_ID], words[BUY_PRODUCT_ID],
      Integer.parseInt(words[BUY_PRICE]), Integer.parseInt(words[BUY_QUANTITY])));
   }

   private static void processEndEntry(final String[] words){
      if (words.length != END_NUM_FIELDS) {
         return;
      }
   }

      //this is called by processFile below - its main purpose is
      //to process the data using the methods you write above
   private static void processLine(
      final String line,
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession)
   {
      final String[] words = line.split("\\h");

      if (words.length == 0)
      {
         return;
      }

      switch (words[0])
      {
         case START_TAG:
            processStartEntry(words, sessionsFromCustomer);
            break;
         case VIEW_TAG:
            // final Map<String, List<Buy>> viewsFromCustomer = new HashMap<>();
            processViewEntry(words, viewsFromSession);
            break;
         case BUY_TAG:
            // final Map<String, List<Buy>> buysFromSession = new HashMap<>();
            processBuyEntry(words, buysFromSession);
            break;
         case END_TAG:
            processEndEntry(words);
            break;
      }
   }


   private static void printAvgViewsWOPurchase(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession) {

            double totalSessionsWOPurchase = 0;
            int totalViewsWOPurchase = 0;
            for(Map.Entry<String, List<String>> entry: sessionsFromCustomer.entrySet()) {
                  List<String> sessions = entry.getValue();

                  for(String sessionID : sessions) {
                        List<Buy> theBuys = buysFromSession.get(sessionID);

                        if (theBuys == null) { // If the customer did not buy anything
                              List<View> theViews = viewsFromSession.get(sessionID);

                              if (theViews != null) {
                                    totalViewsWOPurchase += theViews.size();
                                    totalSessionsWOPurchase++;
                              }
                              else { // Sessions without views count too
                                    totalSessionsWOPurchase++;
                              }
                        }
                  }
            }
            System.out.println("Average Views without Purchase: " + 
                  Double.toString(totalViewsWOPurchase/totalSessionsWOPurchase) + "\n");

      }


   private static void printSessionPriceDifference(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession) {
      

      System.out.println("Price Difference for Purchased Product by Session");

      for(Map.Entry<String, List<String>> entry: sessionsFromCustomer.entrySet()) {
            List<String> sessions = entry.getValue();
            for(String sessionID : sessions) {
                  List<Buy> theBuys = buysFromSession.get(sessionID);
                  List<View> theViews = viewsFromSession.get(sessionID);

                  double avgPrice = 0;
                  
                  if (theViews != null) {
                        double totalPrice = 0;
                        int numViewed = 0;
                        for (View thisView: theViews) {
                              totalPrice += thisView.getPrice();
                              numViewed++;
                        }
                        avgPrice = totalPrice / numViewed;
                  }

                  if (theBuys != null) {
                        System.out.println(sessionID);
                        for (Buy thisBuys: theBuys) {
                              double purchasePrice = thisBuys.getPrice();
                              System.out.println("\t" + (thisBuys.getProductId()) +
                                    " " + Double.toString(purchasePrice - avgPrice));

                        }
                  }
            }
      }
      System.out.println();
   }

      //write this after you have figured out how to store your data
      //make sure that you understand the problem
   private static void printCustomerItemViewsForPurchase(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession) {

      System.out.println("Number of Views for Purchased Product by Customer");

      for (Map.Entry<String, List<String>> entry: sessionsFromCustomer.entrySet()) {
            List<String> sessions = entry.getValue();
            Map<String, Integer> boughtProductsMap = new HashMap<>();
          
            for(String sessionID : sessions) {
                  List<Buy> theBuys = buysFromSession.get(sessionID);
                  if (theBuys != null) {
                        for (Buy thisBuys: theBuys) {
                              boughtProductsMap.put(thisBuys.getProductId(), 0);
                        }
                  }
            }

      // Next go through every session and look for a view that matches bought products
            for (String sessionID : sessions) {
                  ArrayList<String> viewedSessions = new ArrayList<String>();
                  List<View> theViews = viewsFromSession.get(sessionID);

                  if (theViews != null) {
                  for (View thisView: theViews) {
                        if (boughtProductsMap.get(thisView.getProduct()) != null &&
                        !(viewedSessions.contains(thisView.getProduct()))) {
                              boughtProductsMap.put(thisView.getProduct(), (Integer)(boughtProductsMap.get(thisView.getProduct()).intValue() + 1));
                              viewedSessions.add(thisView.getProduct());
                        }
                  }
                  }
            }
            if (!(boughtProductsMap.entrySet().isEmpty())){
                  System.out.println(entry.getKey());
            }
      

            for (Map.Entry<String,Integer> bought : boughtProductsMap.entrySet()) {
                  System.out.println("\t" + bought.getKey() + " " + bought.getValue());
            }
      }
   }

      //write this after you have figured out how to store your data
      //make sure that you understand the problem
   private static void printStatistics(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession
      )
   {
      printAvgViewsWOPurchase(sessionsFromCustomer, viewsFromSession, buysFromSession);
      printSessionPriceDifference(sessionsFromCustomer, viewsFromSession, buysFromSession);
      printCustomerItemViewsForPurchase(sessionsFromCustomer, viewsFromSession, buysFromSession);

      /* This is commented out as it will not work until you read
         in your data to appropriate data structures, but is included
         to help guide your work - it is an example of printing the
         data once propogated 
         printOutExample(sessionsFromCustomer, viewsFromSession, buysFromSession);
      */
		
   }

   /* provided as an example of a method that might traverse your
      collections of data once they are written 
      commented out as the classes do not exist yet - write them! */

   private static void printOutExample(
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession) 
   {
      //for each customer, get their sessions
      //for each session compute views
      for(Map.Entry<String, List<String>> entry: 
         sessionsFromCustomer.entrySet()) 
      {
         System.out.println(entry.getKey());
         List<String> sessions = entry.getValue();
         for(String sessionID : sessions)
         {
            System.out.println("\tin " + sessionID);
            List<View> theViews = viewsFromSession.get(sessionID);
            if (theViews != null) {
                  for (View thisView: theViews) {
                  System.out.println("\t\tlooked " + thisView.getProduct());
                  }
            }
         }
      }
   }


      //called in populateDataStructures
   private static void processFile(
      final Scanner input,
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession)
   {
      while (input.hasNextLine())
      {
         processLine(input.nextLine(), sessionsFromCustomer,
         viewsFromSession, buysFromSession);
      }
   }

      //called from main - mostly just pass through important data structures	
   private static void populateDataStructures(
      final String filename,
      final Map<String, List<String>> sessionsFromCustomer,
      final Map<String, List<View>> viewsFromSession,
      final Map<String, List<Buy>> buysFromSession)
      throws FileNotFoundException
   {
      try (Scanner input = new Scanner(new File(filename)))
      {
         processFile(input, sessionsFromCustomer, viewsFromSession, buysFromSession);
      }
   }

   private static String getFilename(String[] args)
   {
      if (args.length < 1)
      {
         System.err.println("Log file not specified.");
         System.exit(1);
      }

      return args[0];
   }

   public static void main(String[] args)
   {
      /* Map from a customer id to a list of session ids associated with
       * that customer.
       */
      final Map<String, List<String>> sessionsFromCustomer = new HashMap<>();
      final Map<String, List<View>> viewsFromSession = new HashMap<>();
      final Map<String, List<Buy>> buysFromSession = new HashMap<>();
      /* create additional data structures to hold relevant information */
      /* they will most likely be maps to important data in the logs */

      final String filename = getFilename(args);

      try {
         populateDataStructures(filename, sessionsFromCustomer, viewsFromSession,
         buysFromSession);
         printStatistics(sessionsFromCustomer, viewsFromSession, buysFromSession);
      //    printOutExample(sessionsFromCustomer, viewsFromSession, buysFromSession);
      }
      catch (FileNotFoundException e)
      {
         System.err.println(e.getMessage());
      }
   }
}