public class SimpleThreads {

    // Display a message, preceded by the name of the current thread
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop
        implements Runnable {
        public void run() {
            String importantInfo[] = {
                "Mares eat oats",
                "Does eat oats",
                "Little lambs eat ivy",
                "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    // Pause for 4 seconds
                    Thread.sleep(4000);
                    // Print a message
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    private static class PrimeCalculator implements Runnable {
        public void run() {
            long number = 2;
            try {
                while (true) {
                    if (isPrime(number)) {
                        threadMessage(number + " is prime");
                    }
                    number++;
                    // Check if we were interrupted every 1024 iterations
                    if (number % 1024 == 0 && Thread.interrupted()) {
                        threadMessage("Prime calculation interrupted at " + number);
                        throw new InterruptedException();
                    }
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done! Last checked prime: " + (number - 1));
            }
        }

        private boolean isPrime(long n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;
            for (long i = 3; i * i <= n; i += 2) {
                if (n % i == 0) return false;
                // Also check interruption inside heavy computation
                if (i % 256 == 0 && Thread.currentThread().isInterrupted()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String args[])
        throws InterruptedException {

        // Delay, in milliseconds before we interrupt threads (default one hour)
        long patience = 1000 * 60 * 60;

        // If command line argument present, gives patience in seconds
        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer.");
                System.exit(1);
            }
        }

        // --- MessageLoop thread (I/O-bound, same as original) ---
        threadMessage("Starting MessageLoop thread");
        long startTime = System.currentTimeMillis();
        Thread t1 = new Thread(new MessageLoop(), "MessageLoop");
        t1.start();

        // --- PrimeCalculator thread (CPU-intensive) ---
        threadMessage("Starting PrimeCalculator thread");
        Thread t2 = new Thread(new PrimeCalculator(), "PrimeCalculator");
        t2.start();

        threadMessage("Waiting for threads to finish");

        while (t1.isAlive() || t2.isAlive()) {
            threadMessage("Still waiting...");
            t1.join(1000);
            t2.join(1000);
            boolean timedOut = (System.currentTimeMillis() - startTime) > patience;
            if (timedOut) {
                if (t1.isAlive()) {
                    threadMessage("Tired of waiting for MessageLoop!");
                    t1.interrupt();
                    t1.join();
                }
                if (t2.isAlive()) {
                    threadMessage("Tired of waiting for PrimeCalculator!");
                    t2.interrupt();
                    t2.join();
                }
            }
        }
        threadMessage("Finally!");
    }
}
