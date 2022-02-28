package startMain;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import xyz.niflheim.stockfish.StockfishClient;
import xyz.niflheim.stockfish.engine.enums.Option;
import xyz.niflheim.stockfish.engine.enums.Query;
import xyz.niflheim.stockfish.engine.enums.QueryType;
import xyz.niflheim.stockfish.engine.enums.Variant;
import xyz.niflheim.stockfish.exceptions.StockfishInitException;;

public class Game{

    private static final Color lightTileColor = Color.decode("#66CCFF");
    private static final Color darkTileColor = Color.decode("#003366");
    private static String defaultPieceImagesPath = "art/pcs/";

    private static JFrame j;
    private static JMenuBar menu;
    private static GamePanel gamePanel;
    private static JPanel homePanel;
    private static boolean whiteOnMove;
    private static int privremena = -1;
    private static boolean isComputerMove = false;
    private static int numberOfHalfMoves = 0;
    private static int numberOfBlackMoves = 0;
    private static String enPassant = "-";
    private static String rokade = "-";
    private static String stockFishMove;
    private static int sourceTile = -1;
    private static int destinationTile = -1;
    private static StockfishClient client;
    private static int matU3;


    private static boolean pvp = false;
    private static int playerOneScore = 0;
    private static int playerTwoScore = 0;
    private static int brojZavrsenihPartija = 0;
    private static int trenutnoRjesava = 0;

    public Game(){
        try {
            client = new StockfishClient.Builder()
                    .setInstances(4)
                    .setVariant(Variant.BMI2)
                    .setOption(Option.Threads, 3)
                    .setOption(Option.Minimum_Thinking_Time,500)
                    .setOption(Option.Skill_Level, 20)
                    .build();

            j = new JFrame();
            j.setFocusable(true);
            j.setSize(600,600);
            j.setLocationRelativeTo(null);
            j.setResizable(false);
            j.setTitle("ChessProblemSolver");
            j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            homePanel = new JPanel();
            populatePanel();
            j.setContentPane(homePanel);
            setJMenuBar();
            menu.setVisible(false);
            j.setVisible(true);
        }catch(StockfishInitException e){
            System.out.println("Greska pri inicijalizaciji stockfish-a.");
            e.printStackTrace();
        }
    }


    private static void setPvPJMenuBar() {
        menu = new JMenuBar();
        populatePvPJMenuBar();
        menu.setVisible(true);
        j.setJMenuBar(menu);
        j.validate();
    }

    private static void resetPvPStats() {
        pvp = false;
        playerOneScore = 0;
        playerTwoScore = 0;
        brojZavrsenihPartija = 0;
        trenutnoRjesava = 0;
    }
    private static void setJMenuBar() {
        menu = new JMenuBar();
        menu.add(helpMenuButton());
        menu.setVisible(true);
        j.setJMenuBar(menu);
        j.validate();
    }
    private static void setGameJMenuBar() {
        menu = new JMenuBar();
        createGameMenu();
        menu.setVisible(true);
        j.setJMenuBar(menu);
        j.validate();
    }
    private static void populatePvPJMenuBar() {
        final JButton home = new JButton("Pocetna");
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j.setContentPane(homePanel);
                gamePanel = null;
                resetPvPStats();
                menu.setVisible(false);
                j.validate();
            }
        });
        home.setBackground(lightTileColor);
        home.setOpaque(false);
        home.setBorderPainted(false);
        menu.add(home);

        final JButton next = new JButton("Sledeći problem");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(trenutnoRjesava == 1) playerTwoScore++;
                else if(trenutnoRjesava == 2) playerOneScore++;
                nastaviPvP(0);
            }
        });
        next.setBackground(lightTileColor);
        next.setOpaque(false);
        next.setBorderPainted(false);
        menu.add(next);

        final JButton showMove = new JButton("Pomoc");
        showMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(stockFishMove != null &&
                        TilePanel.isValidCoordinate(getCoordinateAtPosition(stockFishMove.substring(2, 4))) &&
                        TilePanel.isValidCoordinate(getCoordinateAtPosition(stockFishMove.substring(0, 2)))){
                    gamePanel.tileArray[getCoordinateAtPosition(stockFishMove.substring(0, 2))].setBackground(Color.decode("#228B22"));
                    gamePanel.tileArray[getCoordinateAtPosition(stockFishMove.substring(2, 4))].setBackground(Color.decode("#228B22"));
                }
            }
        });
        showMove.setBackground(lightTileColor);
        showMove.setOpaque(false);
        showMove.setBorderPainted(false);
        menu.add(showMove);

        String s = "Igrač 1 : " + Integer.toString(playerOneScore) + " | " + "Igrać 2 : " + Integer.toString(playerTwoScore);
        JLabel points = new JLabel(s);
        points.setHorizontalAlignment(SwingConstants.CENTER);

        menu.add(points);

    }

    private static void createGameMenu() {

        final JButton home = new JButton("Pocetna");
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j.setContentPane(homePanel);
                gamePanel = null;
                menu.setVisible(false);
                j.validate();
            }
        });
        home.setBackground(lightTileColor);
        home.setOpaque(false);
        home.setBorderPainted(false);
        menu.add(home);

        final JButton showMove = new JButton("Pomoc");
        showMove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(stockFishMove != null &&
                        TilePanel.isValidCoordinate(getCoordinateAtPosition(stockFishMove.substring(2, 4))) &&
                        TilePanel.isValidCoordinate(getCoordinateAtPosition(stockFishMove.substring(0, 2)))){
                    gamePanel.tileArray[getCoordinateAtPosition(stockFishMove.substring(0, 2))].setBackground(Color.decode("#228B22"));
                    gamePanel.tileArray[getCoordinateAtPosition(stockFishMove.substring(2, 4))].setBackground(Color.decode("#228B22"));
                }
            }
        });
        showMove.setBackground(lightTileColor);
        showMove.setOpaque(false);
        showMove.setBorderPainted(false);
        menu.add(showMove);

    }
    private static JButton helpMenuButton() {
        final JButton b = new JButton("Pocetna");
        b.setBackground(lightTileColor);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j.setContentPane(homePanel);
                menu.setVisible(false);
                j.validate();
            }
        });
        return b;
    }

    public static String getABoard(int x) {
        String temp = "";
        Random rand = new Random();
        int randomLine;
        if(x == 0) randomLine = 1 + rand.nextInt(490);
        else randomLine = 492 + rand.nextInt(100);
        FileReader fr;
        try {
            fr = new FileReader("assets/listOfFen.txt");
            Scanner qwe = new Scanner(fr);
            while( randomLine > 1) {
                qwe.nextLine();
                randomLine--;
            }
            temp = qwe.nextLine();
            qwe.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        return temp;
    }

    public void populatePanel() {
        JLabel label = new JLabel(new ImageIcon("art/finalpic.jpg") );
        homePanel.add(label);
        label.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5,0,0,0);
        gbc.anchor = GridBagConstraints.PAGE_START;

        final JButton mateIn3 = new JButton("Matovi u 3 poteza");
        mateIn3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matU3= 0;
                String m = getABoard(0);
                if(m != null) {
                    gamePanel = new GamePanel(m);
                    j.setContentPane(gamePanel);
                    setGameJMenuBar();
                    j.validate();
                }
            }
        });
        mateIn3.setBorderPainted(false);
        mateIn3.setBackground(Color.decode("#66FFFF"));
        label.add(mateIn3, gbc);

        JButton mateIn4 = new JButton("Matovi u 4 poteza");
        mateIn4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                matU3 = 1;
                String m = getABoard(1);
                if(m != null) {
                    gamePanel = new GamePanel(m);
                    j.setContentPane(gamePanel);
                    setGameJMenuBar();
                    j.validate();
                }

            }
        });
        mateIn4.setBorderPainted(false);
        mateIn4.setBackground(Color.decode("#66FFFF"));
        label.add(mateIn4, gbc);

        JButton pvpButton = new JButton("PvP");
        pvpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pvp = true;
                JOptionPane.showMessageDialog(j, "Igracima se zadaje pozicija. Da bi igrac dobio poene,"
                        + "treba da pritisne, 'p' za Igraca 2 i 'q' za Igraca 1, prije svog protivnika. \n"
                        + "Ako igrac koji rjesava napravi gresku, poen ide protivniku. \n "
                        + "Ukoliko nijedna strana ne zna rjesenje, igraci mogu pritisnuti dugme 'Pomoc' za pomoc ili 'Sledeci problem' da preskoce trenutni problem. \n"
                        + "Igra se sastoji od ukupno 11 problema.", "", JOptionPane.INFORMATION_MESSAGE);

                String[] options = {"Matovi u 3", "Matovi u 4"};
                int op= JOptionPane.showOptionDialog(j,"Izaberite vrstu igre","",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null,options,options[1]);

                j.addKeyListener(new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {}
                    @Override
                    public void keyReleased(KeyEvent e) {}
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if(pvp == true && trenutnoRjesava == 0) {
                            if(e.getKeyChar() == 'q') {
                                trenutnoRjesava = 1;
                            }else if(e.getKeyChar() == 'p') {
                                trenutnoRjesava = 2;
                            }

                            String s2 = "Trenutno rješava: Igrač " + Integer.toString(trenutnoRjesava);
                            JLabel info = new JLabel(s2);
                            info.setHorizontalAlignment(SwingConstants.CENTER);
                            JOptionPane.showMessageDialog(j,info,"",JOptionPane.PLAIN_MESSAGE);

                        }
                    }
                });
                if(op==0)matU3=0;
                else if(op==1)matU3=1;
                String m = getABoard(matU3);
                if(m != null) {
                    gamePanel = new GamePanel(m);
                    j.setContentPane(gamePanel);
                    setPvPJMenuBar();
                    j.validate();}

            }
        });
        pvpButton.setBorderPainted(false);
        pvpButton.setBackground(Color.decode("#66FFFF"));
        label.add(pvpButton, gbc);

        JButton helpButton = new JButton("Pomoc");
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel help = new JPanel();
                help.setBackground(lightTileColor);
                help.setLayout(new BoxLayout(help,BoxLayout.Y_AXIS));
                JTextArea textArea = new JTextArea();
                try {
                    FileReader reader = new FileReader("assets/help.txt");
                    textArea.read(reader, "help.txt");
                    reader.close();
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                textArea.setBackground(lightTileColor);
                textArea.setLineWrap(true);
                textArea.setFont(new Font("TimesRoman", Font.PLAIN, 12));
                textArea.setEditable(false);
                JScrollPane sp = new JScrollPane(textArea);
                help.add(sp);
                j.setContentPane(help);
                setJMenuBar();
                j.validate();
            }
        });
        helpButton.setBorderPainted(false);
        helpButton.setBackground(Color.decode("#66FFFF"));
        label.add(helpButton, gbc);

        JButton quitButton = new JButton("Izlaz");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                j.dispose();
                System.exit(0);
            }
        });
        quitButton.setBorderPainted(false);
        quitButton.setBackground(Color.decode("#66FFFF"));
        label.add(quitButton, gbc);
    }

    //POTEZ
    private static void display() {
        gamePanel = new GamePanel(gamePanel);
        j.setContentPane(gamePanel);
        if(!whiteOnMove)numberOfBlackMoves++;
        whiteOnMove = !whiteOnMove;
        j.validate();
        isComputerMove = true;
        displayAiMove();
    }

    private static void promasenoPolje() {
        if(trenutnoRjesava == 1) playerTwoScore++;
        else if(trenutnoRjesava == 2) playerOneScore++;
        else return;

        nastaviPvP(0);
    }
    private static void nastaviPvP(int x) {
        brojZavrsenihPartija++;
        if(brojZavrsenihPartija == 11 || (playerOneScore == 6 || playerTwoScore == 6)) {
            JLabel checkmate;
            if(playerOneScore > playerTwoScore)checkmate = new JLabel("Igrac 1 je pobjedio!");
            else if(playerOneScore < playerTwoScore)checkmate = new JLabel("Igrac 2 je pobjedio!");
            else checkmate = new JLabel("Remi.");
            resetPvPStats();
            checkmate.setHorizontalAlignment(SwingConstants.CENTER);
            JOptionPane.showMessageDialog(j,checkmate,"",JOptionPane.PLAIN_MESSAGE);
            gamePanel = null;
            whiteOnMove = !whiteOnMove;
            isComputerMove = false;
            sourceTile = -1;
            destinationTile = -1;
            j.setContentPane(homePanel);
            menu.setVisible(false);
            j.validate();
        }else {
            JLabel checkmate;
            if(trenutnoRjesava == 1 && x != 0)checkmate = new JLabel("Poen za Igrača 1!");
            else if(trenutnoRjesava == 2 && x != 0)checkmate = new JLabel("Poen za Igrača 2!");
            else if(trenutnoRjesava == 1 && x == 0)checkmate = new JLabel("Poen za Igrača 2!");
            else if(trenutnoRjesava == 2 && x == 0)checkmate = new JLabel("Poen za Igrača 2!");
            else checkmate = new JLabel("Niko nije dobio poene.");
            trenutnoRjesava = 0;
            checkmate.setHorizontalAlignment(SwingConstants.CENTER);
            JOptionPane.showMessageDialog(j,checkmate,"",JOptionPane.PLAIN_MESSAGE);
            gamePanel = null;
            whiteOnMove = !whiteOnMove;
            isComputerMove = false;
            sourceTile = -1;
            destinationTile = -1;


            String m = getABoard(matU3);
            if(m != null) {
                gamePanel = new GamePanel(m);
                j.setContentPane(gamePanel);
                setPvPJMenuBar();
                j.validate();}
        }
    }
    //AI POTEZ, OPTIMIZOVAN
    private static void displayAiMove() {
        Query query = new Query.Builder(QueryType.Best_Move)
                .setFen(boardToFen())
                .build();
        client.submit(query, result -> {
            if(result.equals("(none)") && pvp == false) {
                JLabel checkmate = new JLabel("Šah mat!");
                checkmate.setHorizontalAlignment(SwingConstants.CENTER);
                String[] options = {"Pocetna","Sledeci problem"};
                int op= JOptionPane.showOptionDialog(j,checkmate,"",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null,options,options[0]);
                if(op==1){
                    String m = getABoard(matU3);
                    if(m != null) {
                        whiteOnMove = !whiteOnMove;
                        stockFishMove = result;
                        isComputerMove = false;
                        sourceTile = -1;
                        destinationTile = -1;
                        gamePanel = new GamePanel(m);
                        j.setContentPane(gamePanel);
                        setGameJMenuBar();
                        j.validate();
                    }
                }else {
                    gamePanel = null;
                    whiteOnMove = !whiteOnMove;
                    stockFishMove = result;
                    isComputerMove = false;
                    sourceTile = -1;
                    destinationTile = -1;
                    j.setContentPane(homePanel);
                    menu.setVisible(false);
                    j.validate();
                }
            }else if(result.equals("(none)") && pvp == true){
                if(trenutnoRjesava == 1) playerOneScore++;
                else if(trenutnoRjesava == 2) playerTwoScore++;
                stockFishMove = result;
                nastaviPvP(1);
            }

            stockFishMove = result;
            sourceTile = getCoordinateAtPosition(result.substring(0, 2));
            destinationTile = getCoordinateAtPosition(result.substring(2, 4));
            gamePanel = new GamePanel(gamePanel);
            j.setContentPane(gamePanel);
            if(!whiteOnMove)numberOfBlackMoves++;
            whiteOnMove = !whiteOnMove;
            j.validate();
            isComputerMove = false;
            Query query2 = new Query.Builder(QueryType.Best_Move)
                    .setFen(boardToFen())
                    .build();
            client.submit(query2, result2 -> {
                stockFishMove = result2;
            });
        });



    }
    //GAME PANEL
    private static class GamePanel extends JPanel{
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public TilePanel[] tileArray;

        public GamePanel(String s){
            this.setLayout(new GridLayout(8,8));
            this.tileArray = new TilePanel[64];
            setFENBoard(s);
        }

        public GamePanel(GamePanel copy) {
            this.setLayout(new GridLayout(8,8));
            this.tileArray = new TilePanel[64];
            if(copy.tileArray[sourceTile].piece.substring(1, 2).equals("K") &&
                    ((sourceTile == 4 && (destinationTile == 2 || destinationTile == 6)) ||
                            (sourceTile == 60 && (destinationTile == 58 || destinationTile == 62)))) {
                if(sourceTile == 4 && (destinationTile == 2 || destinationTile == 6)) {
                    if(destinationTile == 2) {
                        this.tileArray[3] = new TilePanel(3,"BR");
                        this.tileArray[0] = new TilePanel(0,"00");
                    }else{
                        this.tileArray[5] = new TilePanel(5,"BR");
                        this.tileArray[7] = new TilePanel(7,"00");
                    }
                }else if(sourceTile == 60 && (destinationTile == 58 || destinationTile == 62)){
                    if(destinationTile == 58) {
                        this.tileArray[59] = new TilePanel(59,"WR");
                        this.tileArray[56] = new TilePanel(56,"00");
                    }else{
                        this.tileArray[61] = new TilePanel(61,"WR");
                        this.tileArray[63] = new TilePanel(63,"00");
                    }
                }

            }

            if(!enPassant.equals("-") && (getCoordinateAtPosition(enPassant) == destinationTile && gamePanel.tileArray[sourceTile].piece.substring(1, 2).equals("P"))) {
                if(destinationTile > sourceTile) {
                    this.tileArray[getCoordinateAtPosition(enPassant) - 8] = new TilePanel(getCoordinateAtPosition(enPassant),"00");
                }else {
                    this.tileArray[getCoordinateAtPosition(enPassant) + 8] = new TilePanel(getCoordinateAtPosition(enPassant),"00");
                }
            }

            if(gamePanel.tileArray[sourceTile].piece.substring(1, 2).equals("P") && Math.abs(sourceTile-destinationTile) == 16) {
                if(destinationTile > sourceTile) enPassant = getPositionAtCoordinate(destinationTile-8);
                else enPassant = getPositionAtCoordinate(destinationTile + 8);
            }else {

                enPassant = "-";
            }

            boolean neparniRed = true;
            for(int i = 0; i < 64; i++) {
                if(i!=0 && i % 8 == 0) neparniRed = !neparniRed;
                if(i != sourceTile && i != destinationTile) {
                    if(this.tileArray[i] == null)this.tileArray[i] = new TilePanel(i,copy.tileArray[i].piece);
                }else {
                    if(i == sourceTile) this.tileArray[i] = new TilePanel(i,"00");
                    else {
                        if(copy.tileArray[sourceTile].piece.substring(0, 2).equals("WP") && destinationTile < 8) {
                            this.tileArray[i] = new TilePanel(i,"WQ");
                        }else if(copy.tileArray[sourceTile].piece.substring(0, 2).equals("BP") && destinationTile > 55  ) {
                            this.tileArray[i] = new TilePanel(i,"BQ");
                        }else {
                            String s = copy.tileArray[sourceTile].piece.substring(0, 2);
                            this.tileArray[i] = new TilePanel(i,s);
                        }
                    }
                }
                if(neparniRed) {
                    if(i % 2 == 0)tileArray[i].setBackground(lightTileColor);
                    else tileArray[i].setBackground(darkTileColor);
                }else {
                    if(i % 2 == 1)tileArray[i].setBackground(lightTileColor);
                    else tileArray[i].setBackground(darkTileColor);
                }
                if(!this.tileArray[i].piece.equals("00"))this.tileArray[i].assignTilePieceIcon();

                this.add(this.tileArray[i]);
            }

            if(sourceTile == 0) {
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'q')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }else if(sourceTile == 7) {
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'k')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }else if(sourceTile == 56) {
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'Q')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }else if(sourceTile == 63) {
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'K')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }else if(sourceTile == 4) {
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'k' || niz[i] == 'q')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }else if(sourceTile == 60){
                char [] niz = rokade.toCharArray();
                if(niz != null && niz[0] != '-') {
                    rokade = "";
                    for(int i=0;i<niz.length;i++) {
                        if(niz[i] == 'Q' || niz[i] == 'K')continue;
                        else rokade = rokade + niz[i];
                    }
                }
            }

            sourceTile = -1;
            destinationTile = -1;
        }

        public void setFENBoard(String fenNotacija) {
            String unosNiz[] = fenNotacija.split(" ");
            char[] FEN = unosNiz[0].toCharArray();
            char[] NOVI = new char[64];
            int brojacFEN = 0;
            int brojacNOVI = 0;
            while(brojacFEN < FEN.length) {
                if(FEN[brojacFEN] == '/') {
                    brojacFEN++;
                }else if('1' <= FEN[brojacFEN] && FEN[brojacFEN] <= '8') {
                    int vrijednostBroja = Character.getNumericValue(FEN[brojacFEN]);
                    while(vrijednostBroja > 0) {
                        NOVI[brojacNOVI++] = '0';
                        vrijednostBroja--;
                    }
                    brojacFEN++;
                }else{
                    NOVI[brojacNOVI++] = FEN[brojacFEN++];
                }
            }
            boolean neparniRed = true;
            for(int i = 0; i < 64 ; i++) {
                if(i!=0 && i % 8 == 0) neparniRed = !neparniRed;
                if(NOVI[i] == 'p')this.tileArray[i] = new TilePanel(i,"BP");
                else if(NOVI[i] == 'P')this.tileArray[i] = new TilePanel(i,"WP");
                else if(NOVI[i] == 'r')this.tileArray[i] = new TilePanel(i,"BR");
                else if(NOVI[i] == 'R')this.tileArray[i] = new TilePanel(i,"WR");
                else if(NOVI[i] == 'b')this.tileArray[i] = new TilePanel(i,"BB");
                else if(NOVI[i] == 'B')this.tileArray[i] = new TilePanel(i,"WB");
                else if(NOVI[i] == 'n')this.tileArray[i] = new TilePanel(i,"BN");
                else if(NOVI[i] == 'N')this.tileArray[i] = new TilePanel(i,"WN");
                else if(NOVI[i] == 'q')this.tileArray[i] = new TilePanel(i,"BQ");
                else if(NOVI[i] == 'Q')this.tileArray[i] = new TilePanel(i,"WQ");
                else if(NOVI[i] == 'k')this.tileArray[i] = new TilePanel(i,"BK");
                else if(NOVI[i] == 'K')this.tileArray[i] = new TilePanel(i,"WK");
                else if(NOVI[i] == '0')this.tileArray[i] = new TilePanel(i,"00");

                this.add(tileArray[i]);
                if(neparniRed) {
                    if(i % 2 == 0)tileArray[i].setBackground(lightTileColor);
                    else tileArray[i].setBackground(darkTileColor);
                }else {
                    if(i % 2 == 1)tileArray[i].setBackground(lightTileColor);
                    else tileArray[i].setBackground(darkTileColor);
                }
                if(!this.tileArray[i].piece.equals("00"))this.tileArray[i].assignTilePieceIcon();
            }

            if(unosNiz[1].equals("w")) {
                whiteOnMove = true;
                JOptionPane.showMessageDialog(j,   "Bijeli igra i dobija.", "", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                whiteOnMove = false;
                JOptionPane.showMessageDialog(j, "Crni igra i dobija.", "", JOptionPane.INFORMATION_MESSAGE);
            }
            rokade = unosNiz[2];
            enPassant = unosNiz[3];

            Query query = new Query.Builder(QueryType.Best_Move)
                    .setFen(fenNotacija)
                    .build();
            client.submit(query, result -> {
                stockFishMove = result;
            });
        }
    }
    //TILE PANEL, MOOUSE LISTENER
    private static class TilePanel extends JPanel{

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private int position;
        private String piece;
        public TilePanel(int pos, String s) {
            this.position = pos;
            this.piece = s;
            this.setSize(30, 30);
            //MOUSE LISTENER BLOK
            this.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() > 1) return;
                    if(isRightMouseButton(e)) {
                        if(sourceTile == -1);
                        else deSelectTile(sourceTile);
                    }else if(isLeftMouseButton(e) && isComputerMove == false) {
                        if(privremena != -1)deSelectTile(privremena);


                        if(sourceTile == -1 && !piece.equals("00")) {
                            sourceTile = position;
                            if((whiteOnMove == true && gamePanel.tileArray[sourceTile].piece.substring(0, 1).equals("W")) ||
                                    (whiteOnMove == false && gamePanel.tileArray[sourceTile].piece.substring(0, 1).equals("B"))) {
                                gamePanel.tileArray[position].setBackground(Color.DARK_GRAY);
                            }else sourceTile = -1;
                        }else if(sourceTile != -1 && !piece.equals("00")) {
                            //NIJE PRVI KLIK I IMA FIGURE
                            destinationTile = position;
                            if(sourceTile != destinationTile &&
                                    !gamePanel.tileArray[sourceTile].piece.substring(0, 1).equals(gamePanel.tileArray[destinationTile].piece.substring(0, 1)) &&
                                    (sourceTile == getCoordinateAtPosition(stockFishMove.substring(0, 2)) &&
                                            destinationTile == getCoordinateAtPosition(stockFishMove.substring(2, 4)) )) {
                                numberOfHalfMoves = 0;
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        display();
                                    }
                                });
                            }else {
                                boolean u = gamePanel.tileArray[sourceTile].piece.substring(0, 1).equals(gamePanel.tileArray[destinationTile].piece.substring(0, 1));
                                deSelectTile(sourceTile);
                                if(pvp == true && !u) {
                                    promasenoPolje();
                                }
                            }
                        }else if(sourceTile != -1 && piece.equals("00")) {
                            //NIJE PRVI KLIK I NEMA FIGURE
                            destinationTile = position;
                            if(sourceTile == getCoordinateAtPosition(stockFishMove.substring(0, 2)) &&
                                    destinationTile == getCoordinateAtPosition(stockFishMove.substring(2, 4))){
                                numberOfHalfMoves++;
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        display();
                                    }
                                });
                            }else {
                                gamePanel.tileArray[destinationTile].setBackground(Color.red);
                                deSelectTile(sourceTile);
                                if(pvp == true) {
                                    promasenoPolje();
                                }
                            }
                        }//A AKO JE SOURCE TILE==-1 I PIECE == NULL NE RADI NISTA
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
                private boolean isLeftMouseButton(MouseEvent e) {
                    return ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) !=0 || e.getButton() == MouseEvent.BUTTON1);
                }

                private boolean isRightMouseButton(MouseEvent e) {
                    return ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) !=0 || e.getButton() == MouseEvent.BUTTON3);
                }

            });
            //MOUSE LISTENER BLOK KRAJ
        }
        //DESELEKTUJE POLJE
        public void deSelectTile(int i) {
            if(((i/8 == 0 || i/8 == 2 || i/8 == 4 || i/8 == 6) && i % 2 == 0) ||
                    (i % 2 == 1 && (i/8 == 1 || i/8 == 3 || i/8 == 5 || i/8 == 7)))
                gamePanel.tileArray[i].setBackground(lightTileColor);
            else gamePanel.tileArray[i].setBackground(darkTileColor);
            if(i == sourceTile) {
                privremena = destinationTile;
                sourceTile = -1;
                destinationTile = -1;
            }else if(i == privremena) {
                privremena = -1;
            }else {
                throw new RuntimeException("Greska kod deselekcije polja!");
            }
        }
        //DODAJE SLIKU
        private void assignTilePieceIcon() {
            if(piece != null) {
                try {
                    final BufferedImage image =
                            ImageIO.read(new File(defaultPieceImagesPath +
                                    piece.substring(0,1) +
                                    piece.substring(1,2) + ".png"));
                    this.add(new JLabel(new ImageIcon(image)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //PROVJERAVA VALIDNOST KOORDINATE
        private static boolean isValidCoordinate(int x) {
            if(0 <= x && x < 64)return true;
            else return false;
        }
    }
    //POMOCNE FUNKCIJE, ENKAPSULIRANE I OPTIMIZOVANE
    private static String boardToFen() {
        String s = "";
        int broj = 0;
        for(int i = 0 ; i < 64 ;i++) {
            if(i != 0 && i % 8 == 0) {
                if(broj != 0) {
                    s = s+ broj;
                    broj = 0;
                }
                s = s + "/";
            }
            String temp =  gamePanel.tileArray[i].piece.substring(1, 2);
            if(temp.equals("0")) {
                broj++;
            }else if(gamePanel.tileArray[i].piece.substring(0, 1).equals("B")) {
                if(broj != 0) s = s + broj;
                s = s + temp.toLowerCase();
                broj = 0;
            }else {
                if(broj != 0) s = s + broj;
                s = s + temp;
                broj = 0;
            }
        }
        if(broj !=0 )s = s + broj;
        if(whiteOnMove) s = s + " w ";
        else s = s + " b ";

        if(!rokade.equals("-"))s = s + rokade + " ";
        else s = s + "- ";

        s = s + enPassant;

        s = s + " " + numberOfHalfMoves;

        s = s + " " + numberOfBlackMoves + " ";

        return s;
    }

    private static final List<String> CHESS_COORDINATE = initChessCoordinates();

    private static final Map<String, Integer> ARRAY_POSITION = initArrayPosition();

    private static Map<String, Integer> initArrayPosition() {
        final Map<String, Integer> positionToCoordinate = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            positionToCoordinate.put(CHESS_COORDINATE.get(i), i);
        }
        return Collections.unmodifiableMap(positionToCoordinate);
    }

    private static List<String> initChessCoordinates() {
        return Collections.unmodifiableList(Arrays.asList(
                "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
                "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
                "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
                "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
                "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
                "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
                "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
                "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1"));
    }

    public static int getCoordinateAtPosition(final String position) {
        return ARRAY_POSITION.get(position);
    }

    public static String getPositionAtCoordinate(final int coordinate) {
        return CHESS_COORDINATE.get(coordinate);
    }

}
