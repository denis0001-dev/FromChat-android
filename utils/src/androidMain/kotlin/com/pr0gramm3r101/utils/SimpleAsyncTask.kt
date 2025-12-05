@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.pr0gramm3r101.utils

import android.os.AsyncTask

abstract class SimpleAsyncTask: AsyncTask<Unit, Unit, Unit>() {
    final override fun doInBackground(vararg params: Unit) = run()
    abstract fun run()
    abstract fun postRun()
    final override fun onProgressUpdate(vararg values: Unit) {}
    final override fun onPostExecute(result: Unit?) = postRun()
}