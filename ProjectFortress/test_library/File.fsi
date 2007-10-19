(*******************************************************************************
    Copyright 2007 Sun Microsystems, Inc.,
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************)

api File
import * from NativeFile

trait Closeable
    close():()
end

trait FileStream extends Closeable
    getter fileName():String
    getter toString():String
end

(*********************
object ReadStream(filename:String) extends FileStream
    (** Returns the name of the file, as passed in during construction. **)
    getter fileName():String
    getter toString():String
    (** eof returns true if an end-of-file condition has been
        encountered on the stream. **)
    getter eof():Boolean
    (** ready returns true if there is currently input from the stream
        available to be consumed. **)
    getter ready():Boolean

    (** Returns the next available line from the stream, discarding
        line termination characters.  Returns empty string on eof. **)
    readLine():String
    (** Returns the next available character from the stream, or "" on eof. **)
    readChar():Char
    (** Returns the next k characters from the stream.  It will block
        until at least one character is available, and will then
        return as many characters as are ready.  Will return "" on end
        of file.  If k<=0 or absent a default value is chosen. **)
    read(k:ZZ32):String
    read():String

    close():()

    (** All file generators yield file contents in parallel by
        default, with a natural ordering corresponding to the order
        data occurs in the underlying file.  The file is closed if all
        its contents have been read.

        These generators pull in multiple chunks of data (where a
        "chunk" is a line, a character, or a block) before it is
        processed.  The maximum number of chunks to pull in before
        processing is given by the last optional argument in every
        case; if it is <=0 or absent a default value is chosen.

        It is possible to read from a ReadStream before invoking any
        of the following methods.  Previously-read data is ignored,
        and only the remaining data is provided by the generator.

        At the moment it is illegal to read from a ReadStream once any
        of these methods has been called; we do not check for this
        condition.  However, the sequential versions of these
        generators may use label/exit or throw in order to escape from
        a loop, and the ReadStream will remain open and ready for
        reading.

        Once the ReadStream has been completely consumed it is closed.
     **)

    (** lines yields the lines found in the file a la readLine(). **)
    lines(n:ZZ32):FileGenerator[\String\]
    lines():FileGenerator[\String\]

    (** characters yields the characters found in the file a la readChar(). **)
    lines(n:ZZ32):FileGenerator[\String\]
    lines():FileGenerator[\String\]

    (** chunks returns chunks of characters found in the file, in the
        sense of read().  The first argument is equivalent to the
        argument k to read, the second (if present) is the number of
        chunks at a time.
     **)
    chunks(n:ZZ32,m:ZZ32):Generator[\String\]
    chunks(n:ZZ32):Generator[\String\]
    chunks(): Generator[\String\]
end
*************)

fileGenerator[\S\](reader:ReadStream, readOne:ReadStream -> S, upper:ZZ32):
        Generator[\String\]

end
