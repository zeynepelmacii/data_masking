package com.thy.masking.frontend;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.FlowLayout;

public class DesktopApplication extends JFrame {

    Utils utils = new Utils();

    public DesktopApplication() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.getContentPane().setBackground(Color.decode("#242B38"));
        this.setTitle("Raw Data Masking");

        JPanel panelTop = new JPanel();
        panelTop.setLayout(new FlowLayout());
        panelTop.setBackground(Color.decode("#242B38"));

        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new FlowLayout());

        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout());
        panelBottom.setBackground(Color.decode("#242B38"));

        utils.drawParametersPanel(panelTop);
        utils.drawAddRemovePanel(panelTop);
        utils.drawSelectedParametersPanel(panelTop);
        this.add(panelTop);

        utils.drawInfoPanel(panelCenter);
        this.add(panelCenter);

        utils.drawProcessPanel(panelBottom);
        this.add(panelBottom);

        this.setSize(1050, 680);
        this.setVisible(true);
        this.setResizable(false);
    }

}
