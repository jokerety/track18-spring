package ru.track.list;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 *
 * Должен иметь 2 конструктора
 * - без аргументов - создает внутренний массив дефолтного размера на ваш выбор
 * - с аргументом - начальный размер массива
 */
public class MyArrayList extends List
{

    private
        int [] list;


    public MyArrayList()
    {
        list = new int [10];
        capacity_ = 10;
    }

    public MyArrayList(int capacity)
    {
        list = new int [capacity];
        capacity_ = capacity;
    }

    @Override
    void add(int item) {
        if (position >= capacity_)
        {
            if (capacity_ == 0)
            {
                capacity_++;
            }
            capacity_ = capacity_* 2;
            int [] bigList = new int [capacity_ * 2];
            System.arraycopy(list,0,bigList,0,size());
            list = bigList;

        }
        list[position] = item;
        position++;

    }

    @Override
    int remove(int idx) throws NoSuchElementException
    {
        int key = get(idx);
        int j;

        for (j = 0; j < position; j++) //поиск удаляемого элемента
        {
        if (list[j] == key)
            break;
        }
        for (int k = j; k < position - 1; k++) //сдвиг последующих элементов
        {
            list[k] = list[k + 1];
        }
        position--;
        return 0;
    }

    @Override
    int get(int idx) throws NoSuchElementException
    {
        if ((position < 0) || (idx >= position))
        {
            throw new NoSuchElementException();
        }
        return list[idx];
    }


}
