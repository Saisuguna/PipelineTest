import hudson.model.*
def jobsToDelete = []
// For each project
for(item in Hudson.instance.items) {
  // check that job is not building
  if(!item.isBuilding()) {   
    if (item.scm instanceof hudson.plugins.git.GitSCM) {  
      println("Item name: " + item.name);
      item.scm.repositories.each {
        println("Repo " + it.name);
        it.URIs.each {uri ->
          println("URI: " + uri.toString());
          item.scm.branches.each {
            println("Branch " + it.name);
            // Skip empty/regex branch definitions
            if (it.name.length() != 0 && it.name.indexOf("*") > -1) {
              // Use git ls-remote to verify branch existence
              def command = "git ls-remote --heads " + uri + " " + it.name
              def sout = new StringBuffer(), serr = new StringBuffer()
              Process proc = command.execute()
              proc.consumeProcessOutput(sout, serr)
              proc.waitFor()
              println("SOUT: " + sout)
              if (sout.size() == 0) {
                println("Branch does not exist")
                jobsToDelete << item
              }
            }
          

        }

      }
    }
  }
  else {
    println("Skipping job "+item.name+", currently building")
  }
}

jobsToDelete.each {
  println("Job to delete: " + it.name)
  it.delete()
}

