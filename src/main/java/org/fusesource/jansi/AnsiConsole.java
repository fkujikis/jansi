/**
 * Copyright (C) 2009, Progress Software Corporation and/or its 
 * subsidiaries or affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.jansi;

import static org.fusesource.jansi.internal.CLibrary.CLIBRARY;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;

import org.fusesource.jansi.internal.CLibrary;

/**
 * Provides consistent access to an ANSI aware console PrintStream.
 * 
 * @author chirino
 */
public class AnsiConsole {

	private static final PrintStream system_out = System.out;
	public static final PrintStream out = createAnsiConsoleOut();
	private static int installed;
	
	private static boolean stdoutHasNativeAnsiSupport() {
		String os = System.getProperty("os.name");
		if( os.startsWith("Windows") ) {
			return false;
		}		
		return CLIBRARY.isatty(CLibrary.STDOUT_FILENO)!=0;
	}

	private static PrintStream createAnsiConsoleOut() {
		if( stdoutHasNativeAnsiSupport() ) {
			return system_out;
		}
		
		try {
			return new PrintStream(new WindowsAnsiOutputStream(system_out));
		} catch (NoClassDefFoundError ignore) {
			// this happens when JNA is not in the path.
		} catch (IOException e) {
			// this happens when the stdout is not connected to a console.
		}
		
		// Use the ANSIOutputStream to strip out the ANSI escape sequences.
		AnsiOutputStream out = new AnsiOutputStream(system_out);
		return new PrintStream(out);
	}

	/**
	 * If the standard out natively supports ANSI escape codes, then this just 
	 * returns System.out, otherwise it will provide an ANSI aware PrintStream
	 * which strips out the ANSI escape sequences or which implement the escape
	 * sequences.
	 * 
	 * @return a PrintStream which is ANSI aware.
	 */
	public static PrintStream out() {
		return out;
	}
	
	/**
	 * Install Console.out to System.out.
	 */
	synchronized static public void systemInstall() {
		installed++;
		if( installed==1 ) {
			System.setOut(out);
		}
	}
	
	/**
	 * undo a previous {@link #systemInstall()}.  If {@link #systemInstall()} was called 
	 * multiple times, it {@link #systemUninstall()} must call the same number of times before
	 * it is actually uninstalled.
	 */
	synchronized public static void systemUninstall() {
		installed--;
		if( installed==0 ) {
			System.setOut(out);
		}
	}
	
}
