package model;

import java.io.Serializable;

public class Response implements Serializable {
    private boolean success;
    private Object data;

    public Response(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }
    public boolean isSuccess() { return success; }
    public Object getData() { return data; }

}