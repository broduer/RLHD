/*
 * Copyright (c) 2019 Abex
 * Copyright (c) 2022 Mark
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package rs117.hd.utils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import org.pushingpixels.substance.internal.SubstanceSynapse;
import rs117.hd.HdPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public
class WarningMessage {

    private HdPlugin plugin;

    private final JPanel bottomButtons = new JPanel();
    private final Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    private final JPanel rightColumn = new JPanel();
    private final String CONFIG_GROUP = "hd";

    public static List<WarningMessages> messages = new LinkedList<>();

    public void setup(HdPlugin plugin) {
        this.plugin = plugin;
        for (WarningMessages message : WarningMessages.values()) {
            if (plugin.getConfigManager().getConfiguration(CONFIG_GROUP, message.settingKey) == null) {
                plugin.getConfigManager().setConfiguration(CONFIG_GROUP, message.settingKey, false);
                log.info("Unable to find {} Setting to false", message.settingKey);
            }

            if (message.condition.apply(plugin.getConfig()) && plugin.getConfigManager().getConfiguration(CONFIG_GROUP, message.settingKey).equals("false")) {
                messages.add(message);
            }

        }

        if(!messages.isEmpty()) {
            open();
        }

    }

    public void open()
    {
        JDialog dialog = new JDialog();

        try
        {
            BufferedImage logo = ImageUtil.loadImageResource(HdPlugin.class, "logo.png");
            dialog.setIconImage(logo);

            JLabel runelite = new JLabel();
            runelite.setIcon(new ImageIcon(logo));
            runelite.setAlignmentX(Component.CENTER_ALIGNMENT);
            runelite.setBackground(ColorScheme.DARK_GRAY_COLOR);
            runelite.setOpaque(true);
            rightColumn.add(runelite);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }


        dialog.setTitle("117HD Update");
        dialog.setLayout(new BorderLayout());

        JPanel pane = (JPanel) dialog.getContentPane();
        pane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        pane.putClientProperty(SubstanceSynapse.COLORIZATION_FACTOR, 1.0);

        JPanel leftPane = new JPanel();
        leftPane.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        leftPane.setLayout(new BorderLayout());


        StringBuilder text = new StringBuilder();
        for (WarningMessages message : messages) {
            text.append(message.content);
            text.append(System.lineSeparator());
        }

        leftPane.setPreferredSize(new Dimension(400, 200));
        JLabel textArea = new JLabel(text.toString());
        textArea.setFont(font);
        textArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        textArea.setForeground(Color.LIGHT_GRAY);
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        textArea.setOpaque(false);
        textArea.setVerticalAlignment(JLabel.TOP);

        leftPane.add(textArea);

        pane.add(leftPane, BorderLayout.CENTER);

        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setBackground(ColorScheme.DARK_GRAY_COLOR);
        rightColumn.setMaximumSize(new Dimension(200, Integer.MAX_VALUE));

        rightColumn.add(addButton("Got it", () -> {
            messages.forEach(messages -> {
                plugin.getConfigManager().setConfiguration(CONFIG_GROUP,messages.settingKey,true);
            });
            dialog.setVisible(false);
        }));

        rightColumn.add(addButton("Remind me later", () -> dialog.setVisible(false)));
        rightColumn.add(addButton("Discord", () -> LinkBrowser.open("https://discord.gg/U4p6ChjgSE")));

        pane.add(rightColumn, BorderLayout.EAST);



        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);


    }

    public JButton addButton(String message, Runnable action)
    {
        JButton button = new JButton(message);
        button.addActionListener(e -> action.run());
        button.setFont(font);
        button.setBackground(ColorScheme.DARK_GRAY_COLOR);
        button.setForeground(Color.LIGHT_GRAY);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.DARK_GRAY_COLOR.brighter()),
                new EmptyBorder(4, 4, 4, 4)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        button.setFocusPainted(false);
        button.addChangeListener(ev ->
        {
            if (button.getModel().isPressed())
            {
                button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
            else if (button.getModel().isRollover())
            {
                button.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }
            else
            {
                button.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });

        bottomButtons.add(button);
        bottomButtons.revalidate();

        return button;
    }




}
