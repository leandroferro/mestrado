package br.usp.ime.protocol.parser;

import java.util.ArrayList;
import java.util.List;

import br.usp.ime.memnode.ByteArrayWrapper;
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
import br.usp.ime.protocol.command.TryAgainCommand;
import br.usp.ime.protocol.command.WriteCommand;

public abstract class DefaultCommandSerializer implements CommandSerializer {
	
	private static final byte[] _mintransaction = "M".getBytes();
	private static final byte[] _problem = "P".getBytes();
	private static final byte[] _readCommand = "L".getBytes();
	private static final byte[] _writeCommand = "E".getBytes();
	private static final byte[] _command = "C".getBytes();
	private static final byte[] _resultCommand = "R".getBytes();
	private static final byte[] _commitCommand = "S".getBytes();
	private static final byte[] _notCommitCommand = "N".getBytes();
	private static final byte[] _finishCommand = "F".getBytes();
	private static final byte[] _abortCommand = "A".getBytes();
	private static final byte[] _tryAgainCommand = "T".getBytes();
	
	private static final byte[] _space = " ".getBytes();
	private static final byte[] _openCurlyBrace = "{".getBytes();
	private static final byte[] _closeCurlyBrace = "}".getBytes();
	private static final byte[] _newLine = "\n".getBytes();
	
	public static final DefaultCommandSerializer instance = new DefaultCommandSerializer() {
		
		@Override
		public ByteArrayWrapper serialize(Command command) {
			
			if( command == null )
				return new ByteArrayWrapper();
			
			ByteArrayBuilder builder = new ByteArrayBuilder();
			
			if( command instanceof Minitransaction ) {
				Minitransaction minitransaction = (Minitransaction)command;
				
				builder.append(_mintransaction).append(_space).append(minitransaction.getId().length).append(_space);
				
				builder.append(minitransaction.getId());
				
				builder.append(_space).append(_openCurlyBrace).append(_newLine);
				
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
				
				builder.append(serialize(minitransaction.getTryAgainCommand()));
				
				builder.append( _closeCurlyBrace );
			}
			else if (command instanceof Problem) {
				Problem problem = (Problem)command;
				builder.append(_problem).append(_space).append(problem.getDescription().length).append(_space);
				
				builder.append(problem.getDescription());
				
				builder.append(_newLine);
			}
			else if (command instanceof ReadCommand) {
				ReadCommand readCommand = (ReadCommand)command;
				builder.append(_readCommand).append(_space).append(readCommand.getKey().length).append(_space);
				
				builder.append(readCommand.getKey());
				
				builder.append(_newLine);
			}
			else if (command instanceof WriteCommand) {
				WriteCommand writeCommand = (WriteCommand)command;
				builder.append(_writeCommand).append(_space).append(writeCommand.getId().length).append(_space);
				
				builder.append(writeCommand.getId());
				
				builder.append(_space).append(writeCommand.getData().length).append(_space);
				
				builder.append(writeCommand.getData());
				
				builder.append(_newLine);
			}
			else if (command instanceof ExtensionCommand) {
				ExtensionCommand extensionCommand = (ExtensionCommand)command;
				builder.append(_command).append(_space);
				
				builder.append(extensionCommand.getId());
				
				for(Param param : extensionCommand.getParams()) {
					builder.append(_space).append(param.getValue().length).append(_space);
					
					builder.append(param.getValue());
				}
				
				builder.append(_newLine);
			}
			else if (command instanceof ResultCommand) {
				ResultCommand resultCommand = (ResultCommand)command;
				builder.append(_resultCommand).append(_space).append(resultCommand.getId().length).append(_space);
				
				builder.append(resultCommand.getId());
				
				builder.append(_space).append(resultCommand.getData().length).append(_space);
				
				builder.append(resultCommand.getData());
				
				builder.append(_newLine);
			}
			else if (command instanceof CommitCommand) {
				builder.append(_commitCommand).append(_newLine);
			}
			else if (command instanceof NotCommitCommand) {
				builder.append(_notCommitCommand).append(_newLine);
			}
			else if (command instanceof FinishCommand) {
				builder.append(_finishCommand).append(_newLine);
			}
			else if (command instanceof AbortCommand) {
				builder.append(_abortCommand).append(_newLine);
			}
			else if (command instanceof TryAgainCommand) {
				builder.append(_tryAgainCommand).append(_newLine);
			}
			
			return new ByteArrayWrapper(builder.build());
		}
	};
	
	private DefaultCommandSerializer() {
		
	}
	
	public static ByteArrayWrapper serializeCommand(Command command) {
		return instance.serialize(command);
	}
	
	@Override
	public abstract ByteArrayWrapper serialize(Command command);

}
