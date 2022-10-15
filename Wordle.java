package WordleLabs;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Wordle {
    private static final int WIN_WIDTH = 600;
    private static final int WIN_HEIGHT = 600;
    public static void wordle() {
        Window wd = new Window(WIN_WIDTH, WIN_HEIGHT, "20466014 Wordle");
        wd.run(30);
    }
}

class Window {
    public JFrame windowFrame;
    public Board board;
    public TextWindow guesses;
    public Keyboard keyboard;
    public Dictionary words;
    public String wordle;
    Window(int width, int height, String title) {
        words = new Dictionary();
        System.out.println(wordle = words.getWordle() );
        windowFrame = new JFrame();
        windowFrame.setBounds(0, 0, width, height);
        windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        windowFrame.setTitle(title);
        windowFrame.setResizable(false);
        windowFrame.setLayout(new FlowLayout(FlowLayout.CENTER,20,20) );
        windowFrame.setSize(width, height);

        board = new Board(this);
        windowFrame.add(board);

        guesses = new TextWindow(this);
        windowFrame.add(guesses);

        keyboard = new Keyboard(this);
        keyboard.setFocusable(true);
        windowFrame.add(keyboard);

        windowFrame.getContentPane().setBackground(Color.BLACK);
        windowFrame.setVisible(true);
    }

    public void run(int n) {
        int gamesRun = 0;
        while(gamesRun != n) {
            if(board.autoEval(words.getWordle() ) )
                gamesRun++;
            try { Thread.sleep(1000); }
            catch (InterruptedException ignored) {}
        }
    }

    public void restart() {
        board.attempts = 0;
        board.currAttempt.clear();
        for(int i = 0; i < 6; i++) {
            for(int j = 0; j < 5; j++) {
                board.array[i][j] = ' ';
                board.tileColors[i][j] = 0;
            }
        }
        words.resetValid();
        wordle = words.getWordle();
        board.repaint();
        for(JButton button : keyboard.buttons)
            button.setBackground(board.colors[5]);
        guesses.setText("");
    }
}

class Board extends Canvas {
    public char[][] array = new char[6][5];
    public Stack<Character> currAttempt = new Stack<>();
    // 0 = black_empty, 1 = green, 2 = yellow, 3 = black
    public int[][] tileColors = new int[6][5];
    private final Window main;
    // acts as y-axis in evaluations
    int attempts = 0;
    public final Color[] colors = {
            new Color(18,18,19),    // BLACK_EMPTY 0
            new Color(83,141,78),   // GREEN 1
            new Color(181,159,59),  // YELLOW 2
            new Color(58,58,60),    // BLACK 3
            new Color(215,218,220), // WHITE 4
            new Color(129,131,132)  // BUTTON_GRAY 5
    };
    public Board(Window mainIn) {
        main = mainIn;
        setSize(420, 420);
        for(int i = 0; i < 6; i++)
            for(int j = 0; j < 5; j++) {
                array[i][j] = ' ';
                tileColors[i][j] = 0;
            }
        repaint();
    }

    @Override
    public void update(Graphics gr) {
        paint(gr);
    }

    @Override
    public void paint(Graphics gr) {
        Graphics graphics;
        Image image;
        image = createImage(421,421);
        graphics = image.getGraphics();
        graphics.setFont(new Font("Nueue Helvetica 75", Font.PLAIN, 30) );

        // Draw grid
        graphics.setColor(colors[3]);
        for(int lines = 0; lines < 7; lines++) {
            graphics.drawLine(lines*84,0,lines*84,420);
            graphics.drawLine(0,lines*70,420,lines*70);
        }

        // Color grid and insert chars
        for(int y = 0; y < 6; y++) {
            if(y == attempts) {
                for(int x = 0, len = currAttempt.size(); x < 5; x++) {
                    setTileColors(graphics, x, y);
                    if(x < len) { // index out of bound; null stack
                        graphics.setColor(Color.WHITE);
                        graphics.drawString(String.valueOf(currAttempt.get(x) ), x * 84 + 35, y * 70 + 50);
                    }
                }
            } else {
                for(int x = 0; x < 5; x++) {
                    setTileColors(graphics, x, y);
                    graphics.setColor(Color.WHITE);
                    graphics.drawString(String.valueOf(array[y][x]),x * 84 + 35,y * 70 + 50);
                }
            }
        }
        gr.drawImage(image,0,0,this);
    }

    // 0 = BLACK_EMPTY, 1 = GREEN, 2 = YELLOW, 3 = BLACK
    private void setTileColors(Graphics graphics, int x, int y) {
        switch (tileColors[y][x]) {
            case 0 -> {
                graphics.setColor(colors[0]);
                graphics.fillRect(x * 84 + 1, y * 70 + 1, 83, 69);
            }
            case 1 -> {
                graphics.setColor(colors[1]);
                graphics.fillRect(x * 84 + 1, y * 70 + 1, 83, 69);
            }
            case 2 -> {
                graphics.setColor(colors[2]);
                graphics.fillRect(x * 84 + 1, y * 70 + 1, 83, 69);
            }
            case 3 -> {
                graphics.setColor(colors[3]);
                graphics.fillRect(x * 84 + 1, y * 70 + 1, 83, 69);
            }
        }
    }

    void evalCurrRow() {
        if(currAttempt.size() != 5) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input length");
            return;
        }

        String input = "";
        for(char c : currAttempt)
            input += c;

        if(!main.words.wordSearch(input,0,main.words.getAllowableSize()-1) ) {
            JOptionPane.showMessageDialog(this,
                    input + " not found in word list!");
            return;
        }
        if(input.equals(main.wordle) ) {
            for(int i = 0; i < 5; i++)
                tileColors[attempts][i] = 1;
            repaint();
            JOptionPane.showMessageDialog(this,
                    "You've successfully guessed the word in " + (attempts+1) + " attempts!\nClick OK to restart.");
            main.restart();
            return;
        }
        fillBoardArrays(input);
        main.guesses.updateText(input);
        JOptionPane.showMessageDialog(this,
                "Trying " + main.words.getWordle() );
        if(++attempts == 6) {
            repaint();
            JOptionPane.showMessageDialog(this,
                    "Out of tries!\nThe word is " + main.wordle +"\nClick OK to restart.");
            main.restart();
        }
    }

    private void fillBoardArrays(String input) {
        boolean[] used = new boolean[5];
        for(int i = 0; i < 5; i++) {
            if(input.charAt(i) == main.wordle.charAt(i) ) {
                tileColors[attempts][i] = 1;
                array[attempts][i] = input.charAt(i);
                used[i] = true;
            } else {
                boolean found = false;
                for(int j = 0; j < 5; j++) {
                    if(used[j])
                        continue;
                    if(i != j
                            && input.charAt(j) != main.wordle.charAt(j)
                            && input.charAt(i) == main.wordle.charAt(j) ) {
                        tileColors[attempts][i] = 2;
                        array[attempts][i] = input.charAt(i);
                        used[j] = true;
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    tileColors[attempts][i] = 3;
                    array[attempts][i] = input.charAt(i);
                }
            }
        }
        currAttempt.clear();
        main.keyboard.updateKeyboardColors(input, tileColors[attempts]);
        main.words.filterWords(input, tileColors[attempts]);
    }

    boolean autoEval(String input) {
        if(input.equals(main.wordle) ) {
            for(int i = 0; i < 5; i++)
                tileColors[attempts][i] = 1;
            for(int i = 0; i < 5; i++)
                currAttempt.push(input.charAt(i) );
            repaint();
            main.keyboard.updateKeyboardColors(input, tileColors[attempts]);
            main.guesses.gameEndSummary(true);
            main.restart();
            return true;
        }

        fillBoardArrays(input);
        main.guesses.updateText(input);
        repaint();

        if(++attempts == 6) {
            main.keyboard.updateKeyboardColors(input, tileColors[5]);
            main.guesses.gameEndSummary(false);
            main.restart();
            return true;
        }
        return false;
    }
}

class Keyboard extends Panel implements ActionListener {
    private final Window main;
    int index = 0;
    JButton[] buttons = new JButton[30];
    public Keyboard(Window mainIn) {
        main = mainIn;
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        setLayout(new GridBagLayout() );
        setBackground(main.board.colors[3]);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        for(int i = 0; i < 3; i++) {
            c.gridy = i;
            for(int j = 0, len = rows[i].length(); j < len; j++) {
                c.gridx = j;
                add(buttons[index++] =
                        addButton(rows[i].substring(j, j+1), main.board.colors[5], main.board.colors[4]),
                        c
                );
            }
        }
        c.gridx++;
        add(buttons[index++] = addButton("Delete", main.board.colors[5], main.board.colors[4]), c);
        c.gridx++;
        add(buttons[index++] = addButton("Clear", main.board.colors[5], main.board.colors[4]), c);
        c.gridx++;
        add(buttons[index++] = addButton("Enter", main.board.colors[5], main.board.colors[4]), c);
        c.gridy = 1;
        add(buttons[index]   = addButton("Restart", main.board.colors[5], main.board.colors[4]), c);
    }
    private JButton addButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.addActionListener(this);
        return button;
    }
    void updateKeyboardColors(String input, int[] tileColor) {
        for(int i = 0; i < 5; i++) {
            for(int j = 0; j < 26; j++) {
                JButton button = buttons[j];
                if(input.charAt(i) == button.getText().charAt(0) ) {
                    // 0 = gray, 1 = green, 2 = yellow, 3 = black
                    switch(tileColor[i]) {
                        // case 0 -> button.setBackground(main.board.colors[0]);
                        case 1 -> button.setBackground(main.board.colors[1]);
                        case 2 -> {
                            if(button.getBackground() != main.board.colors[1])
                                button.setBackground(main.board.colors[2]);
                        }
                        case 3 -> button.setBackground(main.board.colors[3]);
                    }
                }
            }
        }
        main.keyboard.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == buttons[26]) { // Delete
            if(!main.board.currAttempt.isEmpty() )
                main.board.currAttempt.pop();
        } else if(source == buttons[27]) { // Clear
            main.board.currAttempt.clear();
        } else if(source == buttons[28]) { // Enter
            main.board.evalCurrRow();
        } else if(source == buttons[29]) { // Restart
            main.restart();
        } else {
            for(int i = 0; i < 26; i++) {
                if(source == buttons[i] && main.board.currAttempt.size() < 5) {
                    main.board.currAttempt.push(buttons[i].getText().charAt(0) );
                    break;
                }
            }
        }
        main.board.repaint();
    }
}

class TextWindow extends JTextArea {
    Window main;
    public TextWindow(Window mainIn) {
        main = mainIn;
        setBackground(Color.BLACK);
        setFont(new Font("Nueue Helvetica 75", Font.PLAIN, 15) );
        setLineWrap(true);
        setWrapStyleWord(true);
        setForeground(Color.WHITE);
        setEditable(false);
        setPreferredSize(new Dimension(80,420) );
    }
    public void updateText(String input) {
        this.setText("Trying " + input);
    }
    public void gameEndSummary(boolean won) {
        if(won)
            setText("Word guessed in " + (main.board.attempts+1) + " tries!\n\nNew game in 3 seconds.");
        else
            setText("Out of tries! The word was " + main.wordle + ".\n\nNew game in 3 seconds");
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
    }
}

class Dictionary {
    private ArrayList<String> valid;
    private final String[] allowable;
    private Random rd = new Random();

    public Dictionary() {
        valid = arrListLoad("valid.txt");
        allowable = load("allowable.txt");
    }

    public int getAllowableSize() {
        return allowable.length;
    }

    // Get a valid random word
    public String getWordle() {
        try {
            return valid.get(rd.nextInt(0, valid.size() ) );
        } catch(Exception e) {
            resetValid();
            return valid.get(rd.nextInt(0, valid.size() ) );
        }
    }

    public void resetValid() {
        valid = arrListLoad("valid.txt");
    }

    public String[] load(String file) {
        StringBuilder contents = new StringBuilder();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file) );
            String line;
            while((line = input.readLine() ) != null)
                contents.append(line).append(System.getProperty("line.separator") );
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if(input != null)
                    input.close();
            } catch (IOException ex){
                System.out.println("Input output exception while processing file");
                ex.printStackTrace();
            }
        }
        String[] arr = contents.toString().split("\n");
        for(int i = 0, len = arr.length; i < len; i++)
            arr[i] = arr[i].toUpperCase().trim();
        return arr;
    }

    public ArrayList<String> arrListLoad(String file) {
        ArrayList<String> arr = new ArrayList<>();
        try {
            arr = new BufferedReader(new FileReader(file) )
                    .lines()
                    .collect(Collectors.toCollection(ArrayList::new) );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return arr;
    }

    public boolean wordSearch(String target, int l, int r) {
        if(r < l)
            return false;
        int m = l + (r - l)/2, res = target.compareToIgnoreCase(allowable[m]);
        if(res == 0)
            return true;
        return res > 0
                ? wordSearch(target, m + 1, r)
                : wordSearch(target, l, m - 1);
    }

    // 0 = BLACK_EMPTY, 1 = GREEN, 2 = YELLOW, 3 = BLACK
    public void filterWords(String input, int[] tileColors) {
        HashSet<Character> alphSet = new HashSet<>();
        for(int i = 0; i < 5; i++) {
            char c = input.charAt(i);
            switch(tileColors[i]) {
                case 1 :
                    alphSet.add(c);
                    for(Iterator<String> itr = valid.iterator(); itr.hasNext(); ) {
                        String word = itr.next();
                        if (word.charAt(i) != c) // == c for yellows
                            itr.remove();
                    }
                    break;
                case 2 :
                    alphSet.add(c);
                    for(Iterator<String> itr = valid.iterator(); itr.hasNext(); ) {
                        String word = itr.next();
                        if(word.charAt(i) == c || !word.contains(String.valueOf(c) ) )
                            itr.remove();
                    }
                    break;
                case 3 :
                    if(!alphSet.contains(c) )
                        valid.removeIf(word -> word.contains(String.valueOf(c) ) );
                    break;
            }
        }
    }
}
