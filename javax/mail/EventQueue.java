package javax.mail;

import javax.mail.event.*;
import java.util.*;

class EventQueue implements Runnable
{
    private QueueElement head;
    private QueueElement tail;
    private Thread qThread;
    
    public EventQueue() {
        this.head = null;
        this.tail = null;
        (this.qThread = new Thread(this, "JavaMail-EventQueue")).setDaemon(true);
        this.qThread.start();
    }
    
    public synchronized void enqueue(final MailEvent event, final Vector vector) {
        final QueueElement newElt = new QueueElement(event, vector);
        if (this.head == null) {
            this.head = newElt;
            this.tail = newElt;
        }
        else {
            newElt.next = this.head;
            this.head.prev = newElt;
            this.head = newElt;
        }
        this.notify();
    }
    
    private synchronized QueueElement dequeue() throws InterruptedException {
        while (this.tail == null) {
            this.wait();
        }
        final QueueElement elt = this.tail;
        this.tail = elt.prev;
        if (this.tail == null) {
            this.head = null;
        }
        else {
            this.tail.next = null;
        }
        final QueueElement queueElement = elt;
        final QueueElement queueElement2 = elt;
        final QueueElement queueElement3 = null;
        queueElement2.next = queueElement3;
        queueElement.prev = queueElement3;
        return elt;
    }
    
    public void run() {
        try {
            QueueElement qe;
        Label_0072:
            while ((qe = this.dequeue()) != null) {
                MailEvent e = qe.event;
                Vector v = qe.vector;
                for (int i = 0; i < v.size(); ++i) {
                    try {
                        e.dispatch(v.elementAt(i));
                    }
                    catch (Throwable t) {
                        if (t instanceof InterruptedException) {
                            break Label_0072;
                        }
                    }
                }
                qe = null;
                e = null;
                v = null;
            }
        }
        catch (InterruptedException ex) {}
    }
    
    void stop() {
        if (this.qThread != null) {
            this.qThread.interrupt();
            this.qThread = null;
        }
    }
    
    class QueueElement
    {
        QueueElement next;
        QueueElement prev;
        MailEvent event;
        Vector vector;
        
        QueueElement(final MailEvent event, final Vector vector) {
            this.next = null;
            this.prev = null;
            this.event = null;
            this.vector = null;
            this.event = event;
            this.vector = vector;
        }
    }
}
