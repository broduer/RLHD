package rs117.hd.utils;

import lombok.NonNull;
import lombok.Setter;
import rs117.hd.HdPluginConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public enum WarningMessages {
    MODEL_CACHE("<html>" +
            "As you may have noticed, the 117HD plugin has been updated! This update brings new and improved model caching," +
            "but it's not enabled by default.<br>If you are willing to risk potential crashes or visual artifacts, " +
            "you can enable the cache from the Experimental section of 117HD's settings panel.<br>" +
            "If you run into any issues, please report them in the 117HD Discord server." +
            "</html>", p -> p.configTrue(HdPluginConfig::disableModelCaching)),

    MODEL_CACHE_OYHE("<html>" + "ANOTHER COOL MESSAGE" + "</html>", p -> p.configTrue(HdPluginConfig::disableModelCaching));

    public String content;
    public Function<HdPluginConfig, Boolean> condition;
    public String settingKey = "";

    WarningMessages(String message, Consumer<Builder> consumer) {
        this.content = message;
        Builder builder = new Builder();
        consumer.accept(builder);
        this.condition = builder.condition;
        this.settingKey = "WARNING_" + name() + "_SEEN";
    }

    @Setter
    private static class Builder
    {
        private Function<HdPluginConfig, Boolean> condition;

        Builder configTrue(@NonNull Function<HdPluginConfig, Boolean> condition)
        {
            this.condition = condition;
            return this;
        }

    }

}

