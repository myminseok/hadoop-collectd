
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileMonitor;

public class FileMonitor {

    public static void main(String[] args)throws FileSystemException, InterruptedException {

        FileSystemManager fsManager = VFS.getManager();
        FileObject listendir = fsManager.resolveFile("/Users/myminseok/workspace-git/hadoop-collectd/src/FileMonitor.txt");

        DefaultFileMonitor fm = new DefaultFileMonitor(new CustomFileListener());
        fm.setRecursive(true);
        fm.addFile(listendir);
        fm.start();
        
        while(true){
            
            Thread.sleep(2000);
        }
    }

}

class CustomFileListener implements FileListener{
    @Override
    public void fileChanged(FileChangeEvent event)  throws Exception{
        
        System.out.println(event);
    }

    @Override
    public void fileCreated(FileChangeEvent arg0) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void fileDeleted(FileChangeEvent arg0) throws Exception {
        // TODO Auto-generated method stub
        
    }
    
}
