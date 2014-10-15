import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * This class implements a rather simple, basic Paintball Panic player.
 *
 * Feel free to use the code in this class as a starting point for your own
 * player. In particular, you can use the code to read the game state and write
 * your move to the game engine exactly as given here.
 */
public class PaintballPlayer {

    // flag to say whether we are running in tournament mode or not.  this is based on parameter passed into main()
    private static boolean fisTournament = false;

    // a very simple log (i.e. file) interface.  cannot be used in tournament mode.
    private static PrintWriter sLog;

    // holds information about all the children on the field
    private final Child[] fChildren;

    // scores for each player
    private final int[] fScores;

    // a basic representation of a field
    private final int[][] fField;

    private final Field fieldWithChildren;

    private static final int[] sFieldMapping = {
        Constants.ADAPTER, Constants.BLUE_FLAG, Constants.EMPTY, Constants.EMPTY, Constants.EMPTY,
        Constants.RAPID_FIRE_LAUNCHER, Constants.EMPTY, Constants.EMPTY, Constants.EMPTY, Constants.EMPTY,
        Constants.EMPTY, Constants.BASIC_LAUNCHER, Constants.EMPTY, Constants.EMPTY, Constants.EMPTY,
        Constants.EMPTY, Constants.EMPTY, Constants.RED_FLAG, Constants.SHIELD, Constants.TREE,
        Constants.EMPTY, Constants.LOW_WALL, Constants.HIGH_WALL, Constants.EMPTY, Constants.EMPTY, Constants.EMPTY
    };

    /**
     * Construct a basic player object.
     */
    private PaintballPlayer() {
        fChildren = new Child[Constants.NUM_CHILDREN_PER_TEAM * 2];

        fScores = new int[2];

        fField = new int[Constants.FIELD_DIMENSION][Constants.FIELD_DIMENSION];

        fieldWithChildren = new Field(fField, fChildren);
        Route.setField(fieldWithChildren);

        fChildren[0] = new Scout(Constants.RED, fieldWithChildren);
        for (int i = 1; i < fChildren.length; i++) {
            fChildren[ i] = new Fighter(i < Constants.NUM_CHILDREN_PER_TEAM ? Constants.RED : Constants.BLUE, fieldWithChildren);
        }
    }

    /**
     * The main player run loop.
     */
    public void run() {
        // Scanner to parse input from the game engine.
        Scanner in = new Scanner(System.in);

        // Keep reading states until the game ends.
        int turnNumber = in.nextInt();

        // the game engine sends a -1 for a turn number when the game is over
        while (turnNumber >= 0) {
//            if (turnNumber % 5 == 0) {
//                fieldWithChildren.log();
//            }
            // Read current game score.
            fScores[ Constants.RED] = in.nextInt();
            fScores[ Constants.BLUE] = in.nextInt();

            // Read the current field configuration and store in the field contents array
            for (int i = 0; i < Constants.FIELD_DIMENSION; i++) {
                for (int j = 0; j < Constants.FIELD_DIMENSION; j++) {
                    // Decode the field space encoding
                    String fieldEncoding = in.next();
                    fField[ i][ j] = decodeFieldSymbol(fieldEncoding);
                }
            }

            // Read the states of all the children.
            for (Child child : fChildren) {
                String encoding = in.next();
                if (encoding.equals("*")) {
                    // if the encoding is just a *, then it means we can't see this child.
                    // this could only happen for children on the other team
                    child.setPosition(new Point());
                } else {
                    // we can see this child.

                    // Record the child's location.
                    int x = Integer.parseInt(encoding);
                    int y = in.nextInt();

                    child.setPosition(new Point(x, y));

                    // read posture ( standing = S, crouching = C )
                    encoding = in.next();
                    child.setIsStanding(encoding.equals("S"));

                    // read defending mode ( defending = D, not defending = U )
                    encoding = in.next();
                    child.setIsDefending(encoding.equals("D"));

                    // read inventory
                    encoding = in.next();
                    child.setHolding(encoding.charAt(0) - 'a');

                    // read the number of paintballs the child is holding
                    child.setPaintballCount(in.nextInt());
                }
            }

            fieldWithChildren.update(fField);

            // Decide what each child should do
            for (int i = 0; i < Constants.NUM_CHILDREN_PER_TEAM; i++) {
                Action action;
                try {
                    action = fChildren[ i].chooseAction();
                } catch (Exception e) {
//                    LOG.log(Level.SEVERE, "Error choosing Action!", e);
                    action = Action.makeIdleAction();
                }

                if (!fisTournament) {
                    sLog.println(action.toString());
                }

                // write the child's action to the game engine
                System.out.println(action.toString());
            }

            turnNumber = in.nextInt();
        }
    }

    /**
     * This function maps a field encoding string to an integer constant value
     * to store in the field map.
     *
     * @param fieldEncoding the encoding read from the game engine
     *
     * @return the integer code representing the space's contents
     */
    private static int decodeFieldSymbol(String fieldEncoding) {
        char code = fieldEncoding.charAt(0);

        if (code == '*') {
            return 10;
        } else if (code == '.') {
            // a * means that we cannot see what is at that field location
            // a . means the space is empty
            return 0;
        } else if (code == 'P') {
            // an encoding that starts with P represents a pile of paintballs
            // we store the number of paintballs in the space as a negative value (the value is the number
            // of paintballs) to distinguish from the other space encodings
            return -Integer.parseInt(fieldEncoding.substring(1));
        } else {
            // use a simple array to map the code to an integer encoding
            return sFieldMapping[ code - 'A'];
        }
    }

    /**
     * This is the main entry point to the player.
     *
     * @param args command-line arguments tournament: this argument is passed
     * when the player is run as part of the official tournament website no file
     * or network I/O is allowed
     */
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                fisTournament = args[ 0].equals("tournament");
            }

            if (!fisTournament) {
                sLog = new PrintWriter(new FileWriter(String.format("sp-%d.log", (int) (Math.random() * 1000))));
            }

            PaintballPlayer player = new PaintballPlayer();

            player.run();
        } catch (IOException t) {
            System.err.format("Unhandled exception %s\n", t.getMessage());
            if (!fisTournament) {
                t.printStackTrace(sLog);
            } else {
                t.printStackTrace();
            }
        }

        if (!fisTournament) {
            sLog.close();
        }

        // explicitly exit with a success status
        System.exit(0);
    }

//    private static final Logger LOG = Logger.getLogger("1306B");
//
//    static {
//        FileHandler fh;
//
//        try {
//
//            // This block configure the LOG with handler and formatter  
//            fh = new FileHandler("/Users/Finn/Downloads/PaintballPanic-1.1/debug.log");
//            LOG.addHandler(fh);
//            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
//
//        } catch (SecurityException | IOException e) {
//            LOG.log(Level.SEVERE, "Cannot create Logger", e);
//        }
//    }
}
