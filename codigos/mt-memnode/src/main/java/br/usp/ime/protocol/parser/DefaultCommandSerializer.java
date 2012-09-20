package br.usp.ime.protocol.parser;

import br.usp.ime.protocol.command.AbortCommand;
import br.usp.ime.protocol.command.Command;
import br.usp.ime.protocol.command.CommitCommand;
import br.usp.ime.protocol.command.ExtensionCommand;
import br.usp.ime.protocol.command.FinishCommand;
import br.usp.ime.protocol.command.Minitransaction;
import br.usp.ime.protocol.command.NotCommitCommand;
import br.usp.ime.protocol.command.Param;
import br.usp.ime.protocol.command.Problem;
import br.usp.ime.protocol.command.ReadCommand;
import br.usp.ime.protocol.command.ResultCommand;
import br.usp.ime.protocol.command.WriteCommand;

public abstract class DefaultCommandSerializer implements CommandSerializer {

	public static final DefaultCommandSerializer instance = new DefaultCommandSerializer() {
		
		@Override
		public String serialize(Command command) {
			
			if( command == null )
				return "";
			
			StringBuilder builder = new StringBuilder();
			
			if( command instanceof Minitransaction ) {
				Minitransaction minitransaction = (Minitransaction)command;
				
				builder.append("M").append(" ").append(minitransaction.getId().length).append(" ");
				
				for(byte b : minitransaction.getId()) {
					builder.append((char)b);
				}
				
				builder.append(" ").append("{").append("\n");
				
				builder.append( serialize(minitransaction.getProblem()) );
				
				for( ExtensionCommand extensionCommand : minitransaction.getExtensionCommands() ) {
					builder.append( serialize(extensionCommand) );
				}
				
				for( ReadCommand readCommand : minitransaction.getReadCommands() ) {
					builder.append( serialize(readCommand) );
				}
				
				for( WriteCommand writeCommand : minitransaction.getWriteCommands() ) {
					builder.append( serialize(writeCommand) );
				}
				
				for( ResultCommand resultCommand : minitransaction.getResultCommands() ) {
					builder.append( serialize(resultCommand) );
				}
				
				builder.append(serialize(minitransaction.getCommitCommand()));
				
				builder.append(serialize(minitransaction.getNotCommitCommand()));
				
				builder.append(serialize(minitransaction.getFinishCommand()));
				
				builder.append(serialize(minitransaction.getAbortCommand()));
				
				builder.append( "}" );
			}
			else if (command instanceof Problem) {
				Problem problem = (Problem)command;
				builder.append("P").append(" ").append(problem.getDescription().length).append(" ");
				
				for(byte b : problem.getDescription()) {
					builder.append((char)b);
				}
				
				builder.append("\n");
			}
			else if (command instanceof ReadCommand) {
				ReadCommand readCommand = (ReadCommand)command;
				builder.append("L").append(" ").append(readCommand.getKey().length).append(" ");
				
				for(byte b : readCommand.getKey()) {
					builder.append((char)b);
				}
				
				builder.append("\n");
			}
			else if (command instanceof WriteCommand) {
				WriteCommand writeCommand = (WriteCommand)command;
				builder.append("E").append(" ").append(writeCommand.getId().length).append(" ");
				
				for(byte b : writeCommand.getId()) {
					builder.append((char)b);
				}
				
				builder.append(" ").append(writeCommand.getData().length).append(" ");
				
				for(byte b : writeCommand.getData()) {
					builder.append((char)b);
				}
				
				builder.append("\n");
			}
			else if (command instanceof ExtensionCommand) {
				ExtensionCommand extensionCommand = (ExtensionCommand)command;
				builder.append("C").append(" ");
				
				for(byte b : extensionCommand.getId()) {
					builder.append((char)b);
				}
				
				for(Param param : extensionCommand.getParams()) {
					builder.append(" ").append(param.getValue().length).append(" ");
					
					for(byte b : param.getValue()) {
						builder.append((char)b);
					}
				}
				
				builder.append("\n");
			}
			else if (command instanceof ResultCommand) {
				ResultCommand resultCommand = (ResultCommand)command;
				builder.append("R").append(" ").append(resultCommand.getId().length).append(" ");
				
				for(byte b : resultCommand.getId()) {
					builder.append((char)b);
				}
				
				builder.append(" ").append(resultCommand.getData().length).append(" ");
				
				for(byte b : resultCommand.getData()) {
					builder.append((char)b);
				}
				
				builder.append("\n");
			}
			else if (command instanceof CommitCommand) {
				builder.append("S").append("\n");
			}
			else if (command instanceof NotCommitCommand) {
				builder.append("N").append("\n");
			}
			else if (command instanceof FinishCommand) {
				builder.append("F").append("\n");
			}
			else if (command instanceof AbortCommand) {
				builder.append("A").append("\n");
			}
			
			return builder.toString();
		}
	};
	
	private DefaultCommandSerializer() {
		
	}
	
	public static String serializeCommand(Command command) {
		return instance.serialize(command);
	}
	
	@Override
	public abstract String serialize(Command command);

}
