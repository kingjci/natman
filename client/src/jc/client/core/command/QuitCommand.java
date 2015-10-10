package jc.client.core.command;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class QuitCommand implements Command {

    private String reason;

    public QuitCommand(String reason){
        this.reason = new String(reason);
    }

    public String getReason(){
        return reason;
    }

    @Override
    public String getCommandType() {
        return "QuitCommand";
    }
}
