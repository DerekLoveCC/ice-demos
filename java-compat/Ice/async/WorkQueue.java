//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

import Demo.*;

public class WorkQueue extends Thread
{
    class CallbackEntry
    {
        AMD_Hello_sayHello cb;
        int delay;
    }

    @Override
    public synchronized void
    run()
    {
        while(!_done)
        {
            if(_callbacks.size() == 0)
            {
                try
                {
                    wait();
                }
                catch(java.lang.InterruptedException ex)
                {
                }
            }

            if(!_done && _callbacks.size() != 0)
            {
                //
                // Get next work item.
                //
                CallbackEntry entry = _callbacks.getFirst();

                //
                // Wait for the amount of time indicated in delay to
                // emulate a process that takes a significant period of
                // time to complete.
                //
                try
                {
                    wait(entry.delay);
                }
                catch(java.lang.InterruptedException ex)
                {
                }

                if(!_done)
                {
                    //
                    // Print greeting and send response.
                    //
                    _callbacks.removeFirst();
                    System.err.println("Belated Hello World!");
                    entry.cb.ice_response();
                }
            }
        }

        //
        // Throw exception for any outstanding requests.
        //
        for(CallbackEntry p : _callbacks)
        {
            p.cb.ice_exception(new RequestCanceledException());
        }
    }

    public synchronized void
    add(AMD_Hello_sayHello cb, int delay)
    {
        if(!_done)
        {
            //
            // Add the work item.
            //
            CallbackEntry entry = new CallbackEntry();
            entry.cb = cb;
            entry.delay = delay;

            if(_callbacks.size() == 0)
            {
                notify();
            }
            _callbacks.add(entry);
        }
        else
        {
            //
            // Destroyed, throw exception.
            //
            cb.ice_exception(new RequestCanceledException());
        }
    }

    public synchronized void
    _destroy()                  // Thread.destroy is deprecated.
    {
        _done = true;
        notify();
    }

    private java.util.LinkedList<CallbackEntry> _callbacks = new java.util.LinkedList<CallbackEntry>();
    private boolean _done = false;
}
