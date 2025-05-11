package util;

import java.io.Serializable;

public class Message implements Serializable
{
    private String type;
    private Object args;

    public Message(String type, Object args)
    {
        this.type = type;
        this.args = args;
    }

    public String getType()
    {
        return type;
    }

    public Object getArgs()
    {
        return args;
    }
}

