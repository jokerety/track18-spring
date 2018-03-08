package ru.track.list;

import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 * Односвязный список
 */
public class MyLinkedList extends List implements Stack, Queue{

    /**
     * private - используется для сокрытия этого класса от других.
     * Класс доступен только изнутри того, где он объявлен
     * <p>
     * static - позволяет использовать Node без создания экземпляра внешнего класса
     */
    private

        Node root;
        Node tail;

    @Override
    public void push(int value) {
        add(value);
    }

    @Override
    public int pop() {
        return remove(position-1);
    }

    @Override
    public void enqueue(int value) {
        add(value);
    }

    @Override
    public int dequeu() {
        return remove(0);
    }

    private static class Node {
        Node prev;
        Node next;
        int val;

        Node(Node prev, Node next, int val) {
            this.prev = prev;
            this.next = next;
            this.val = val;
        }
    }

    @Override
    void add(int item)
    {
        if (root == null)
        {
            root = new Node (null, null, item);
            tail = root;

        }
        else
        {
            Node addNode = new Node (tail, null, item);
            tail.next = addNode;
            tail = addNode;

        }
        position++;

    }

    @Override
    int remove(int idx) throws NoSuchElementException
    {
        int i;
        if ((position < 0) || (idx >= position))
        {
            throw new NoSuchElementException();
        }
        Node found =  root;
        for (i = 0 ; i < idx;i++)
        {
            found = found.next;
        }
        int result = found.val;
        if ((found.prev == null) && (found.next == null))
        {
        }
        else if ((found.prev == null) && (found.next != null))
        {
            root = found;
        }
        else if ((found.next == null) && (found.prev != null))
        {
            tail = found;
        }
        else
        {
            found.prev.next = found.next;
            found.next.prev = found.prev;
        }


        found.next = found.prev = null;

        position--;
        return result;
    }

    @Override
    int get(int idx) throws NoSuchElementException
    {
        int i;
        if (position == 0)
        {
            throw new NoSuchElementException();
        }
        Node found =  root;
        for (i = 0 ; i < idx;i++)
        {
            found = found.next;
        }
        return found.val;
    }


}
