package com.models;


import com.twilio.sdk.TwilioRestException;
import utilities.TwilioUtils;

public class Todo {

    private String title;
    private String body;
    private boolean done;

    public Todo() {
        this("", "", false );
    }

    public Todo( String title, String body, boolean done ) {
        this.title = title;
        this.body = body;
        this.done = done;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getBody() {

        return body;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public boolean isDone() {

        return done;
    }

    public void setDone(boolean done) {

        boolean doneBeforeState = this.done;
        this.done = done;
        //send SMS to notify that the task has been marked done
        if( doneBeforeState == false && done == true ) {
            String message = title + " has been marked as done";
            try {
                TwilioUtils.sendSms("+14158198840",
                        "+19376014128", message);
            } catch (TwilioRestException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Todo)) return false;

        Todo todo = (Todo) o;

        if (done != todo.done) return false;
        if (body != null ? !body.equals(todo.body)
                         : todo.body != null) return false;
        if (title != null ? !title.equals(todo.title)
                          : todo.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (done ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo { " +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", done=" + done + ' ' +
                '}';
    }
}
