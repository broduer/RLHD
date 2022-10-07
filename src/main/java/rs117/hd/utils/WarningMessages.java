package rs117.hd.utils;

import lombok.NonNull;
import lombok.Setter;
import rs117.hd.HdPluginConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public enum WarningMessages {
    MODEL_CACHE("As you may have noticed, the 117HD plugin has been updated!<br>" +
            "<br>" +
            "This update brings new and improved model caching, but it's not enabled by default.<br>" +
            "If you are willing to risk potential crashes or visual artifacts, you can enable the<br>" +
            "new cache from the Experimental section of 117HD's settings panel.<br>" +
            "<br>" +
            "If you experience any issues, please report them in the <a href=\"https://discord.gg/U4p6ChjgSE\">117HD Discord server</a>."

            // comment out this block of text to toggle between little and a lot of text
            +
            "<br><br>Mark thinks multiple issues will be a problem, but look at this!<br>" +
            "<br>Seems quite reasonable.<br>" +
            "<br>Btw, you should be using 64-bit. 32-bit can lead to horrible performance.<br>" +
            "<br>" +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed mattis lacus nec vulputate consequat. Ut ullamcorper massa hendrerit pulvinar ultrices. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Aenean nibh dolor, posuere a convallis nec, posuere a erat. Integer eu quam semper, condimentum magna vitae, congue erat. Phasellus pellentesque at ex at iaculis. Nunc a facilisis ipsum. Aenean eget blandit enim. Quisque ac lacus in quam fringilla rhoncus. Praesent finibus nisl vel feugiat ultrices. Cras suscipit sapien ut massa dictum efficitur. Phasellus at elementum nisl. Duis tristique dignissim sodales. Quisque sit amet tempus nulla. Maecenas vitae porta dui, in cursus odio.<br>" +
            "<br>" +
            "Duis eget hendrerit eros. Pellentesque non massa efficitur, pharetra turpis sit amet, aliquam magna. Nunc ligula leo, fringilla in auctor eget, pellentesque sit amet augue. Phasellus rhoncus justo vel efficitur rhoncus. Ut lobortis dolor leo, nec dapibus dolor dapibus eu. Morbi a tellus ut elit porttitor vestibulum ut nec diam. Nullam porttitor, risus ut gravida ultricies, velit orci aliquam leo, ut posuere lacus risus quis massa. In ullamcorper, velit a tincidunt rhoncus, metus urna sollicitudin quam, ut porta mauris lorem ac felis. Vivamus vitae mi vel enim bibendum varius.",
    p -> p.configTrue(HdPluginConfig::disableModelCaching)),

    MODEL_CACHE_OYHE("ANOTHER COOL MESSAGE", p -> p.configTrue(HdPluginConfig::disableModelCaching));

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

