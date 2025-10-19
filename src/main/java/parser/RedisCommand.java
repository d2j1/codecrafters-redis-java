package parser;

import java.util.List;

public class RedisCommand {
    private String name;
    private List<String> args;

    public RedisCommand(String name, List<String> args){
        this.name = name;
        this.args = args;
    }

    public String getName(){
        return this.name;
    }

public List<String> getArgs(){
        return this.args;
}


}
