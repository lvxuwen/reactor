package reactor.io.file;

import reactor.fn.Consumer;
import reactor.io.Buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Consumer} implementation that dumps everything it accepts to an open {@link FileChannel}.
 *
 * @author Jon Brisbin
 */
public class FileChannelConsumer<T> implements Consumer<T> {

	private final File             outFile;
	private final FileOutputStream out;
	private final FileChannel      channel;
	private final AtomicLong position = new AtomicLong(0);

	/**
	 * Create a new timestamped file in the given directory, using the given basename. A timestamp will be append to the
	 * basename so no files will ever be overwritten using this {@link Consumer}.
	 *
	 * @param dir      The directory in which to create the file.
	 * @param basename The basename to use, to which will be appended a timestamp.
	 * @throws FileNotFoundException
	 */
	public FileChannelConsumer(String dir, String basename) throws FileNotFoundException {
		File d = new File(dir);
		if (!d.exists()) {
			d.mkdirs();
		}

		String filename = basename + "-" + System.currentTimeMillis();
		this.outFile = new File(d, filename);
		this.out = new FileOutputStream(outFile);
		this.channel = out.getChannel();
	}

	/**
	 * Get the {@link File} object for the currently-open {@link FileChannel}.
	 *
	 * @return The {@link File} for this channel.
	 */
	public File getFile() {
		return outFile;
	}

	@Override
	public void accept(T t) {
		try {
			Buffer b = Buffer.wrap(t.toString());
			channel.write(b.byteBuffer(), position.getAndAdd(b.remaining()));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
