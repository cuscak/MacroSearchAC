package cuscak.ac;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;

public class MacroSearchAC extends Frame implements ActionListener{

    private final String REC_FILE_EXTENSION = "REC";

    private final int REC_ADMIN_FIELDS = 15;
    private final int NREC_ADMIN_FIELDS = 21;

    private Choice fileExtField;
    private JTextField macroField;
    private JTextField valueField;
    private String outFileName;
    private JFileChooser jFileChooser;

    private String directory;

    public MacroSearchAC(){
        super("Macro Search for SUE");   //create the window

        // Destroy the window when the user requests it
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10,0,0,0);  //top padding

        JLabel fileExtLabel = new JLabel("REC or NREC: ");
        JLabel macroLabel = new JLabel("Macro number: ");
        JLabel valueLabel = new JLabel("What value: ");
        JLabel dirLabel = new JLabel("Choose directory: ");

        fileExtField = new Choice();
        fileExtField.add("REC");
        fileExtField.add("NREC");

        macroField = new JTextField(10);
        valueField = new JTextField(20);

        jFileChooser = new JFileChooser(System.getProperty("user.dir"));
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.addActionListener(this);

        gc.fill = GridBagConstraints.HORIZONTAL;    //REC or NREC label
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(fileExtLabel, gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //REc or NREC chooser
        gc.gridx = 1;
        gc.gridy = 0;
        panel.add(fileExtField, gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //Macro label
        gc.gridx = 0;
        gc.gridy = 1;
        panel.add(macroLabel,gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //macro text field
        gc.gridx = 1;
        gc.gridy = 1;
        panel.add(macroField,gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //value label
        gc.gridx = 0;
        gc.gridy = 2;
        panel.add(valueLabel,gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //value text field
        gc.gridx = 1;
        gc.gridy = 2;
        gc.ipady = 20;
        panel.add(valueField,gc);

        gc.ipady = 0;       //set height to default

        gc.fill = GridBagConstraints.HORIZONTAL;    //file chooser label
        gc.gridx = 0;
        gc.gridy = 3;
        panel.add(dirLabel,gc);

        gc.fill = GridBagConstraints.HORIZONTAL;    //file chooser obj
        gc.gridx = 1;
        gc.gridy = 3;
        panel.add(jFileChooser,gc);

        JButton b = new JButton("Submit");      //submit btn
        b.addActionListener(this);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 1;
        gc.gridwidth = 2;   //2 columns wide
        gc.gridy = 4;
        panel.add(b,gc);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        this.add(panel);
        this.setSize(700,600);
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean macroProvided = false;
        boolean valueProvided = false;
        boolean dirProvided = false;
        int macro = -999;   //initializing macro ro dummy value
        String value = null;

        String extension = fileExtField.getSelectedItem();

        if (macroField.getText().isEmpty()){
            JOptionPane.showMessageDialog(this,"You did not provide any MACRO number","No Macro", JOptionPane.ERROR_MESSAGE);
        }else{
            try{
                macro = Integer.parseInt(macroField.getText());
                macroProvided = true;
            }catch (NumberFormatException exception){
                JOptionPane.showMessageDialog(this,"MACRo can be only a NUMBER","NOT a NUMBER", JOptionPane.ERROR_MESSAGE);
            }
        }

        if(valueField.getText().trim().isEmpty() & macroProvided){
            JOptionPane.showMessageDialog(this,"What VALUE to search for?","Empty VALUE", JOptionPane.WARNING_MESSAGE);
        }else{
            value = valueField.getText().trim();
            valueProvided = true;
        }

        if(jFileChooser.getSelectedFile() == null & macroProvided & valueProvided){
            JOptionPane.showMessageDialog(this,"Select ROOT folder to do the search in","No folder selected", JOptionPane.WARNING_MESSAGE);
        }else{
            directory = jFileChooser.getSelectedFile().toString();
            dirProvided = true;
        }

        int transformedMacro = checkAdminfileds(macro, extension);

        outFileName = String.valueOf(System.currentTimeMillis()) + "_Macro-" + macro + "_Value-" + value +"_em." + extension;

        if(macroProvided & valueProvided & dirProvided){
            doSearch(extension, transformedMacro, value);
            //dialog window to say that job is done and closing the  app
            JOptionPane.showMessageDialog(this,"\tFinished. \n Check current folder for a file with results","Job finished",JOptionPane.PLAIN_MESSAGE);
            System.exit(0);
        }
    }

    private void doSearch(String extension, int macro, String value) {

        //Path directory = Paths.get("").toAbsolutePath();

        MacroSearchWorkerAC msw = new MacroSearchWorkerAC(extension, macro, value);
        try {
            Files.walkFileTree(Paths.get(directory), msw);
            ArrayList<String> result = msw.getOutputToWrite();

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))){
                for (String s:result) {
                    writer.write(s);
                    writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Something wrong with writing to the file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Something wrong with looping through files");
            e.printStackTrace();
        }
    }

    private int checkAdminfileds(int macroToCheck, String filterToCheck) {
        if(filterToCheck.toUpperCase().equals(REC_FILE_EXTENSION) && macroToCheck > REC_ADMIN_FIELDS){
            macroToCheck += REC_ADMIN_FIELDS;
        }else if(macroToCheck > NREC_ADMIN_FIELDS){   //means we are in NREC
            macroToCheck += NREC_ADMIN_FIELDS;
        }
        return macroToCheck -= 1;     //remove 1 because of 0 based indexing
    }

    public static void main(String[] args) {
        MacroSearchAC msg = new MacroSearchAC();
        msg.setVisible(true);
    }
}
