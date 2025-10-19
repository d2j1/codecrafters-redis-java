package parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RESPParser {

    // returns command and its arguments list
    public List<String> parse(InputStream input) throws IOException {

        input.mark(1);
        int firstByte = input.read();

        if(firstByte == -1){
            return null; // end of stream
        }

        char prefix = (char) firstByte;

        switch(prefix){
            case '*': // array
                return parseArray(input);
            case '$': // bulk string
                return Collections.singletonList(parseBulkString(input));
            case '+': // simple string
                return Collections.singletonList(readLine(input));
            default:
                throw new IOException("Unsupported RESP type: "+ prefix);
        }
    }

    private String readLine(InputStream input) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int b;

        while( ( b= input.read()) != -1){

            if( b=='\r'){

                int next = input.read();
                if( next == '\n') {
                    break; // end
                }
                else { // not end - false alarm
                    buffer.write(b);
                    buffer.write(next);
                }
                }else{
                buffer.write(b);
            }
        }

        if( b == -1) throw new IOException("Unexpected end of stream while reading line");
        return buffer.toString();

    }

    // bulk string starts with $
    private String parseBulkString(InputStream input) throws IOException {

        String lenline = readLine(input);
        int length = Integer.parseInt(lenline);

        if(length == -1){
            return null; // null string
        }

        byte[] buf = new byte[length];
        int read=0;

        // this will keep reading until EOF
        while(read < length){
            int r = input.read( buf, read, length-read);
            if( r==-1) throw new IOException("Unexpected end of stream while reading bulk string");
            read += r;
        }

        // consume last \r and \n

        int cr = input.read();
        int lf = input.read();

        if( cr != '\r' || lf != '\n') throw new IOException("Invalid Bulk String ending");

        return new String(buf);
    }

    // array starts with *
    private List<String> parseArray(InputStream input) throws IOException{

        String countLine = readLine(input);
        int count = Integer.parseInt(countLine);

        List<String> elements = new ArrayList<>();

        for( int i =0; i < count; i++){
            input.mark(1);

            int next = input.read();

            if( next == -1){
                throw  new IOException("Unexcepted end of stream inside array");
            }

            char prefix = (char) next;

            switch (prefix){
                case '$':
                    elements.add(parseBulkString(input));
                    break;
                case '+':
                    elements.add(readLine(input));
                    break;
                case '*':
                    throw new IOException("Nested arrays are not supported yet!");
                default:
                    throw new IOException("Unsupported RESP type inside array: "+ prefix);
            }
        }

        return elements;
    }
}
