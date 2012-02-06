/* License (BSD Style License):
*  Copyright (c) 2009, 2011
*  Software Technology Group
*  Department of Computer Science
*  Technische Universität Darmstadt
*  All rights reserved.
*
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*  - Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
*  - Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
*  - Neither the name of the Software Technology Group or Technische 
*    Universität Darmstadt nor the names of its contributors may be used to 
*    endorse or promote products derived from this software without specific 
*    prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
*  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
*  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
*  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
*  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
*  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
*  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
*  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
*  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
*  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
*  POSSIBILITY OF SUCH DAMAGE.
*/
package de.tud.cs.st.clusters
package framework
package util

import java.io.File
import structure.util.DefaultClusterManager
import de.tud.cs.st.bat.resolved.dependency.DependencyExtractor
import de.tud.cs.st.bat.resolved.reader.Java6Framework
import de.tud.cs.st.bat.resolved.DoNothingSourceElementsVisitor
import java.io.FileWriter
import java.io.BufferedWriter

/**
 * @author Thomas Schlosser
 *
 */
trait ConsoleParameterValidator {

    protected def validateOutputFileParameter(outputFile: java.io.File, parameterName: String) {
        if (outputFile == null)
            println("Parameter '"+parameterName+"' is not allowed to be empty in this case. Please choose a file as '"+parameterName+"'.")
        else if (!outputFile.exists)
            outputFile.createNewFile()
        if (outputFile.isDirectory)
            println("Output can not be written to a directory! Please choose a file as '"+parameterName+"'.")
        else if (!outputFile.canWrite)
            println("It is not allowed to write to the given output file! Please choose a writable file as '"+parameterName+"'.")
        else return
        sys.exit(1)
    }

    protected def validateInputFileParameter(inputFile: java.io.File, parameterName: String) {
        if (inputFile == null)
            println("Parameter '"+parameterName+"' is not allowed to be empty in this case. Please choose a file as '"+parameterName+"'.")
        else if (!inputFile.exists)
            println("Input file does not exist! Please choose an existing file as '"+parameterName+"'.")
        if (inputFile.isDirectory)
            println("Given input is a directory! Please choose a file as '"+parameterName+"'.")
        else if (!inputFile.canRead)
            println("It is not allowed to read from the given file! Please choose a readable file as '"+parameterName+"'.")
        else return
        sys.exit(1)
    }
}