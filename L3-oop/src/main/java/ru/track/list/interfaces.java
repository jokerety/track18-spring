package ru.track.list;


    interface Stack
    {
        void push(int value); // положить значение наверх стека
        int pop(); // вытащить верхнее значение со стека
    }

    // Очередь - структура данных, удовлетворяющая правилу First IN First OUT
    interface Queue
    {
        void enqueue(int value); // поместить элемент в очередь
        int dequeu(); // вытащить первый элемент из очереди
    }

