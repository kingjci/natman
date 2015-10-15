package jc.command;

/**
 * Created by ��� on 2015/9/23.
 */
public class QuitCommand implements Command {

    private String reason; // reason for exiting
    private int exitCode; //
    private String sourceThread; // which thread the command come from

    public QuitCommand(String sourceThread,String reason, int exitCode){
        this.sourceThread = sourceThread;
        this.reason = new String(reason);
        this.exitCode = exitCode;
    }

    public String getReason(){
        return reason;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String getCommandType() {
        return "QuitCommand";
    }

    @Override
    public String getSourceThread() {
        return null;
    }
}
