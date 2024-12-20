import java.util.*;
import java.util.concurrent.*;

// Основной класс системы управления лифтами
class ElevatorSystem {
    private final List<Elevator> elevators; // Список лифтов
    private final BlockingQueue<Request> requests; // Очередь заявок

    // Конструктор системы управления лифтами
    public ElevatorSystem(int elevatorCount) {
        elevators = new ArrayList<>();
        requests = new LinkedBlockingQueue<>();
        for (int i = 1; i <= elevatorCount; i++) {
            elevators.add(new Elevator(i));
        }
    }

    // Запуск системы управления лифтами
    public void start() {
        // Запуск потоков для каждого лифта
        for (Elevator elevator : elevators) {
            new Thread(elevator).start();
        }
        // Запуск потока генерации заявок
        new Thread(this::generateRequests).start();
    }

    // Генерация заявок от пользователей
    private void generateRequests() {
        Random random = new Random();
        while (true) {
            int sourceFloor = random.nextInt(10) + 1; // Генерация этажа вызова (1-10)
            int destinationFloor = random.nextInt(10) + 1; // Генерация этажа назначения (1-10)
            if (sourceFloor != destinationFloor) {
                Request request = new Request(sourceFloor, destinationFloor);
                requests.offer(request); // Добавление заявки в очередь
                System.out.println("New request: from floor " + sourceFloor + " to floor " + destinationFloor);
            }
            try {
                Thread.sleep(2000); // Создание новой заявки каждые 2 секунды
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Класс лифта, который обрабатывает заявки
    class Elevator implements Runnable {
        private final int id; // Идентификатор лифта
        private int currentFloor; // Текущий этаж
        private final List<Request> tasks; // Список текущих задач лифта

        public Elevator(int id) {
            this.id = id;
            this.currentFloor = 1; // Лифт начинается с первого этажа
            this.tasks = new ArrayList<>();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // Проверка очереди заявок и добавление задачи
                    if (tasks.isEmpty() && !requests.isEmpty()) {
                        synchronized (requests) {
                            tasks.add(requests.poll());
                        }
                    }

                    // Обработка задач лифта
                    if (!tasks.isEmpty()) {
                        tasks.sort(Comparator.comparingInt(req -> Math.abs(req.getSourceFloor() - currentFloor))); // Сортировка задач по расстоянию до текущего этажа
                        Request task = tasks.remove(0);
                        moveToFloor(task.getSourceFloor()); // Движение к этажу вызова
                        System.out.println("Elevator " + id + " picked up passenger at floor " + task.getSourceFloor());
                        moveToFloor(task.getDestinationFloor()); // Движение к этажу назначения
                        System.out.println("Elevator " + id + " dropped off passenger at floor " + task.getDestinationFloor());
                    } else {
                        Thread.sleep(1000); // Ожидание, если задач нет
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Метод для перемещения лифта на указанный этаж
        private void moveToFloor(int floor) throws InterruptedException {
            while (currentFloor != floor) {
                if (currentFloor < floor) {
                    currentFloor++;
                } else {
                    currentFloor--;
                }
                System.out.println("Elevator " + id + " is at floor " + currentFloor);
                Thread.sleep(500); // Имитация времени перемещения между этажами
            }
        }
    }

    // Класс заявки на использование лифта
    static class Request {
        private final int sourceFloor; // Этаж вызова
        private final int destinationFloor; // Этаж назначения

        public Request(int sourceFloor, int destinationFloor) {
            this.sourceFloor = sourceFloor;
            this.destinationFloor = destinationFloor;
        }

        public int getSourceFloor() {
            return sourceFloor;
        }

        public int getDestinationFloor() {
            return destinationFloor;
        }
    }

    // Основной метод программы
    public static void main(String[] args) {
        // Создание системы с 3 лифтами
        ElevatorSystem system = new ElevatorSystem(3);
        system.start(); // Запуск системы
    }
}

/*
Описание работы программы:
1. Программа моделирует работу системы управления лифтами в многоквартирном доме.
2. В системе одновременно работают 3 лифта, каждый из которых обрабатывает свои задачи.
3. Заявки на вызов лифта генерируются случайным образом каждые 2 секунды.
4. Лифты минимизируют время ожидания, обрабатывая ближайшие к ним вызовы.
5. Логи работы лифтов выводятся в консоль, включая информацию о текущем этаже, обработанных заявках и перемещениях лифта.
*/
