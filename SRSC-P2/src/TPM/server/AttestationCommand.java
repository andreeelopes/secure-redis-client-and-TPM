package TPM.server;

public class AttestationCommand {
	
	private String command;
	private int[] columns;
	
	
	public AttestationCommand(String command, int[] columns) {
		this.command = command;
		this.columns = columns;
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	
	public int[] getColumns() {
		return columns;
	}
	public void setColumns(int[] columns) {
		this.columns = columns;
	}
}
