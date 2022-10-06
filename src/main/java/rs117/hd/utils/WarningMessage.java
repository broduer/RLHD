package rs117.hd.utils;

import lombok.extern.slf4j.Slf4j;
import rs117.hd.HdPlugin;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public
class WarningMessage {

    public static List<String> messages = new LinkedList();

    public static void setup(HdPlugin plugin) {
        for (WarningMessages message : WarningMessages.values()) {
            if (plugin.getConfigManager().getConfiguration("hd", message.settingKey) == null) {
                plugin.getConfigManager().setConfiguration("hd", message.settingKey, false);
                log.info("Unable to find {} Setting to false", message.settingKey);
            }

            if (message.condition.apply(plugin.getConfig()) && plugin.getConfigManager().getConfiguration("hd", message.settingKey) != "true") {
                messages.add(message.message);
            }

        }
        System.out.println(messages.size());


    }

}
