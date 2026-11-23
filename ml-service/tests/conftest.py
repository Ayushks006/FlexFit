"""
Pytest configuration and global fixtures for FlexFit ML Service.
"""

import os
import sys
import time
import _thread
import threading
import traceback
import pytest


@pytest.fixture(autouse=True)
def enforce_test_timeout():
    """
    Reliable watchdog mechanism to prevent tests from hanging indefinitely.
    
    1. First attempts a Python-level interrupt via `_thread.interrupt_main()`.
    2. Dumps the stack frames of all active threads for debugging.
    3. If the thread remains blocked inside a C-level system call (e.g., Winsock accept)
       that ignores Python signals, forces process termination via `os._exit()`.
    """
    timeout_seconds = 5.0
    main_thread_id = threading.get_ident()

    def timeout_handler():
        sys.stderr.write(
            f"\n[WATCHDOG ERROR] Test exceeded {timeout_seconds}s execution limit!\n"
            "Dumping thread stacks:\n"
        )
        for thread_id, frame in sys._current_frames().items():
            sys.stderr.write(f"\n--- Thread ID: {thread_id} ---\n")
            traceback.print_stack(frame, file=sys.stderr)
        sys.stderr.flush()

        # Step 1: Attempt Python-level interrupt
        try:
            _thread.interrupt_main()
        except Exception:
            pass

        # Step 2: Grace period (0.5s) for Python exception handler to respond
        time.sleep(0.5)

        # Step 3: Hard abort if thread is stuck in an un-interruptible C-level kernel call
        sys.stderr.write(
            "[WATCHDOG ERROR] Process still alive after signal; force-terminating via os._exit(124).\n"
        )
        sys.stderr.flush()
        os._exit(124)

    timer = threading.Timer(timeout_seconds, timeout_handler)
    timer.daemon = True
    timer.start()
    try:
        yield
    finally:
        timer.cancel()
