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
import net.runelite.api.Constants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.LinkBrowser;
import org.pushingpixels.substance.internal.SubstanceSynapse;
import rs117.hd.HdPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static rs117.hd.utils.ResourcePath.path;

@Slf4j
public
class WarningMessage {

    private HdPlugin plugin;
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
        SwingUtilities.invokeLater(() -> {
            final int maxWidth = 400;
            final int maxHeight = 360;

            JFrame frame = new JFrame("117HD Update");
            frame.setMinimumSize(new Dimension(400, 200));
            JPanel framePanel = new JPanel();
            framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.PAGE_AXIS));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 8, 8));

            try {
                BufferedImage logoImage = path(HdPlugin.class, "logo.png").loadImage();
                frame.setIconImage(logoImage);
                Image logoScaled = logoImage.getScaledInstance(96, -1, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(logoScaled));
                logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                mainPanel.add(logoLabel, BorderLayout.LINE_START);
            } catch (IOException ex) {
                log.error("Unable to load HD logo: ", ex);
            }

            StringBuilder text = new StringBuilder();
            for (WarningMessages message : messages) {
                text.append(message.content);
                text.append(System.lineSeparator());
            }

            System.out.println(text);

            // TODO: this is one way to make words wrap, but it forces a certain width
//			String html = String.format("<html><body style=\"width: %dpx\">%s</body></html>", maxWidth - 150, message);
            String html = String.format("<html>%s</html>", text);
            JLabel messageLabel = new JLabel(html);
            messageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            JScrollPane scrollPane = new JScrollPane(messageLabel);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setPreferredSize(new Dimension(
                    Math.min(scrollPane.getPreferredSize().width, maxWidth),
                    Math.min(scrollPane.getPreferredSize().height, Math.min(messageLabel.getPreferredSize().height, maxHeight))
            ));
            scrollPane.setMaximumSize(new Dimension(maxWidth, maxHeight));
            scrollPane.setPreferredSize(new Dimension(50, 50));
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));

            buttonPanel.add(new JButton("dfd"));
            buttonPanel.add(new JButton("dfd1"));

            framePanel.add(mainPanel);
            framePanel.add(buttonPanel);

            frame.setContentPane(framePanel);
            frame.pack();
            frame.setLocationRelativeTo(plugin.getClient().getCanvas());
            Point point = frame.getLocation();
            frame.setLocation(point.x, point.y + (Constants.GAME_FIXED_HEIGHT - plugin.getClient().getCanvasHeight()) / 2 - 10);
            frame.setAutoRequestFocus(true);

            JFrame runeLiteWindow = (JFrame) SwingUtilities.getWindowAncestor(plugin.getClient().getCanvas());
            if (runeLiteWindow.isAlwaysOnTop())
                frame.setAlwaysOnTop(true);

            frame.setVisible(true);
        });
    }



}
