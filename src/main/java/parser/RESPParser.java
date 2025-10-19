package parser;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        return buffer.toString(StandardCharsets.UTF_8);

    }

    // bulk string starts with $
    private String parseBulkString(InputStream input) throws IOException {
        // Read the length line (after $)
        String lenline = readLine(input);
        int length = Integer.parseInt(lenline);

        if (length == -1) return null; // null string

        if (length < -1) throw new IOException("Invalid bulk string length: " + length);

        byte[] buf = new byte[length];
        int read = 0;

        while (read < length) {
            int r = input.read(buf, read, length - read);
            if (r == -1) throw new IOException("Unexpected end of stream while reading bulk string");
            read += r;
        }

        // Consume trailing \r\n
        int cr = input.read();
        int lf = input.read();
        if (cr != '\r' || lf != '\n') throw new IOException("Invalid Bulk String ending");

        return new String(buf, StandardCharsets.UTF_8);
    }

    // array starts with *
    private List<String> parseArray(InputStream input) throws IOException {
        // Read the count line (after '*')
        String countLine = readLine(input);
        int count = Integer.parseInt(countLine);

        List<String> elements = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            input.mark(1);
            int prefixByte = input.read();
            if (prefixByte == -1) throw new IOException("Unexpected end of stream inside array");

            char prefix = (char) prefixByte;

            switch (prefix) {
                case '$': // Bulk string
                    elements.add(parseBulkString(input));
                    break;

                case '+': // Simple string reply
                    elements.add(readLine(input));
                    break;

                case '*': // Nested arrays (optional for now)
                    throw new IOException("Nested arrays not supported yet");

                default:
                    throw new IOException("Unsupported RESP type inside array: " + prefix);
            }
        }

        return elements;
    }

}
