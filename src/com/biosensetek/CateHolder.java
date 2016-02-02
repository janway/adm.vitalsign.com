package com.biosensetek;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.common.util.NamedList;

public class CateHolder implements Serializable {
	private static final long serialVersionUID = -3251284700302096774L;

	//@formatter:off
	private transient static final String SYS_CHECK = new StringBuilder()
	.append("SELECT IFNULL((SELECT MAX(up)a FROM cate WHERE status=1),0)c")
	.append(      ",IFNULL((SELECT MAX(up)a FROM itemcate WHERE status=1")
	.append(                 " AND EXISTS(SELECT 0 FROM item WHERE status>0 AND uid=itemcate.item)")
	.append(                 " AND EXISTS(SELECT 0 FROM cate WHERE status=1 AND uid=itemcate.cate)")
	.append(              "),0)i")
	.toString();
	
	private transient static final String SYS_QUERY = new StringBuilder()
	.append("SELECT uid i,display d,name n,pid p")
	.append(      ",(SELECT COUNT(0)")
	.append(          "FROM itemcate")
	.append(        " WHERE status=1 AND cate=cate.uid")
	.append(          " AND EXISTS(SELECT 0 FROM item WHERE status>0 AND uid=itemcate.item)")
	.append(       ")c")
	.append( " FROM cate")
	.append(" WHERE status=1")
	.append(" ORDER BY level,seq,cr")
	.toString();

	// 4 parameters
	private transient static final String SHOP_CHECK  = new StringBuilder()
	.append("SELECT IFNULL((SELECT MAX(up)a FROM shopcate WHERE status=1 AND shop=?),0)c")
	.append(      ",IFNULL((SELECT MAX(up)a")
	.append(                " FROM itemshopcate")
	.append(               " WHERE status=1 AND shop=?")
	.append(                 " AND EXISTS(SELECT 0 FROM item WHERE status>0 AND uid=itemshopcate.item AND shop=?)")
	.append(                 " AND EXISTS(SELECT 0 FROM shopcate WHERE status=1 AND uid=itemshopcate.shopcate AND shop=?)")
	.append(              "),0)i")
	.toString();
	
	// 3 parameters
	private transient static final String SHOP_QUERY = new StringBuilder()
	.append("SELECT uid i,display d,name n,pid p")
	.append(      ",(SELECT COUNT(0)")
	.append(          "FROM itemshopcate")
	.append(        " WHERE status=1 AND shopcate=shopcate.uid AND shop=?")
	.append(          " AND EXISTS(SELECT 0 FROM item WHERE status>0 AND uid=itemshopcate.item AND shop=?)")
	.append(       ")c")
	.append( " FROM shopcate")
	.append(" WHERE status=1 AND shop=?")
	.append(" ORDER BY level,seq,cr")
	.toString();
	//@formatter:on

	private static final long expire = TimeUnit.MINUTES.toMillis(10);

	private static CateHolder sys = new CateHolder();
	private static long lastModified = -1L;

	public static final CateHolder sys() {
		sys.x.lock();
		//@formatter:off
		if (System.currentTimeMillis() - lastModified > expire) {
			boolean update = sys.roots == null;
			if (update) {
				sys.roots = new ArrayList<>(); sys.reference = new HashMap<>(); sys.mapping = new HashMap<>();
			} else {
				try (Connection conn = DBUtils.conn("main")) {
					try (Statement stat = conn.createStatement()) {
						try (ResultSet rs = stat.executeQuery(SYS_CHECK)) {
							if (rs.next()) {
								long time;
								if (sys.lastC != (time = rs.getTimestamp(1).getTime())) {
									sys.lastC = time; update = true;
								}
								if (sys.lastI != (time = rs.getTimestamp(2).getTime())) {
									sys.lastI = time; update = true;
								}
							}
						}
					}
				} catch (Throwable e) {}
			}
			if (update) {
				// 非最佳化的狀況下，結果仍可能有誤差
				ExecutorService executor = Executors.newFixedThreadPool(2);
				Future<NamedList<Object>> itemLuke = null, shopLuke = null;
				if (item != null || shop != null) {
					LukeRequest luke = new LukeRequest();
					luke.addField("scateno");// 視實際增減此值，要保持大於實際系統分類之總數
					luke.setNumTerms(10000); 
					if (item != null) {
						itemLuke = executor.submit(() -> {
							try { return get("topTerms", get("scateno", get("fields", item.request(luke)))); } catch (Throwable e) {}
							return null;
						});
					}
					if (shop != null) {
						shopLuke = executor.submit(() -> {
							try { return get("topTerms", get("scateno", get("fields", shop.request(luke)))); } catch (Throwable e) {}
							return null;
						});
					}
				}
				sys.roots.clear(); sys.reference.clear(); sys.mapping.clear(); Cate cate, find; String pid;
				try (Connection conn = DBUtils.conn("main")) {
					try (Statement stat = conn.createStatement()) {
						try (ResultSet rs = stat.executeQuery(SYS_QUERY)) {
							while (rs.next()) {
								if ((pid = rs.getString("p")) == null) {
									sys.roots.add(cate = new Cate());
								} else if ((find = sys.reference.get(pid)) == null) {
									continue;
								} else {
									find.children(cate = new Cate());
								}
								sys.mapping.put(cate.display(rs.getString("d")).display(), cate);
								sys.reference.put(cate.uid(rs.getString("i")).uid(), cate);
								cate.name(rs.getString("n")).items(rs.getInt("c"));
							}
						}
					}
				} catch (Throwable e) {}
				sys.roots.forEach(Cate::check);
				ArrayList<Future<?>> futures = new ArrayList<>(2);
				if (itemLuke != null) {
					Future<NamedList<Object>> future = itemLuke;
					itemLuke = null;
					futures.add(executor.submit(() -> {
						NamedList<Object> terms = null;
						try { terms = future.get(); } catch (Throwable t) {}
						if (terms == null) return;
						terms.forEach(term -> {
							Cate c = sys.mapping.get(term.getKey());
							if (c == null) return;
							int val = (int) term.getValue();
							if (c.items() != val) c.items(val);
						});
					}));
				}
				if (shopLuke != null) {
					Future<NamedList<Object>> future = shopLuke;
					shopLuke = null;
					futures.add(executor.submit(() -> {
						NamedList<Object> terms = null;
						try { terms = future.get(); } catch (Throwable t) {}
						if (terms == null) return;
						terms.forEach(term -> {
							Cate c = sys.mapping.get(term.getKey());
							if (c == null) return;
							int val = (int) term.getValue();
							if (c.shops() != val) c.shops(val);
						});
					}));
				}
				while (futures.size() > 0) {
					futures.removeIf(Future::isDone);
					futures.removeIf(Future::isCancelled);
				}
				executor.shutdown();
			}
		}
		//@formatter:on
		sys.x.unlock();
		return sys;
	}

	private static HttpSolrServer item = null, shop = null;

	public static final HttpSolrServer itemSolr(HttpSolrServer solr) {
		return item = solr;
	}

	public static final HttpSolrServer shopSolr(HttpSolrServer solr) {
		return shop = solr;
	}

	@SuppressWarnings("unchecked")
	private static final NamedList<Object> get(String name, NamedList<Object> target) {
		return (NamedList<Object>) target.get(name);
	}

	private static final HashMap<String, CateHolder> shops = new HashMap<>();
	private static final Lock shopx = new Lock();
	private static final int queue = 10;

	public static final CateHolder shop(String shop) {
		if (shop == null) return null;
		//
		CateHolder holder = null;
		String b64 = Utils.base64uuid(shop);
		shopx.lock();
		if (shops.containsKey(b64)) {
			holder = shops.get(b64);
		} else {
			shops.put(b64, holder = new CateHolder());
		}
		holder.access();
		shopx.unlock();
		//
		holder.x.lock();
		//@formatter:off
		File cache = new File(SystemUtils.JAVA_IO_TMPDIR, b64 + "cate");
		if (cache.exists() && System.currentTimeMillis() - lastModified < expire) {
			holder.copy(read(cache));
		}
		boolean update = holder.roots == null;
		if (update) {
			holder.roots = new ArrayList<>(); holder.reference = new HashMap<>(); holder.mapping = new HashMap<>();
		} else {
			try (Connection conn = DBUtils.conn("main")) {
				try (PreparedStatement stat = conn.prepareStatement(SHOP_CHECK)) {
					for (int i = 1; i < 5; i++) stat.setString(i, shop);
					try (ResultSet rs = stat.executeQuery()) {
						if (rs.next()) {
							long time;
							if (holder.lastC != (time = rs.getTimestamp(1).getTime())) {
								holder.lastC = time; update = true;
							}
							if (holder.lastI != (time = rs.getTimestamp(2).getTime())) {
								holder.lastI = time; update = true;
							}
						}
					}
				}
			} catch (Throwable e) {}
		}
		if (update) {
			holder.roots.clear(); holder.reference.clear(); holder.mapping.clear(); Cate cate, find; String pid;
			try (Connection conn = DBUtils.conn("main")) {
				try (PreparedStatement stat = conn.prepareStatement(SHOP_QUERY)) {
					for (int i = 1; i < 4; i++) stat.setString(i, shop);
					try (ResultSet rs = stat.executeQuery()) {
						while (rs.next()) {
							if ((pid = rs.getString("p")) == null) {
								holder.roots.add(cate = new Cate());
							} else if ((find = holder.reference.get(pid)) == null) {
								continue;
							} else {
								find.children(cate = new Cate());
							}
							holder.mapping.put(cate.display(rs.getString("d")).display(), cate);
							holder.reference.put(cate.uid(rs.getString("i")).uid(), cate);
							cate.name(rs.getString("n")).items(rs.getInt("c"));
						}
					}
				}
			} catch (Throwable e) {}
			holder.roots.forEach(Cate::check);
			write(cache, holder);
		}
		//@formatter:on
		holder.x.unlock();
		//
		shopx.lock();
		if (shops.size() > queue) {
			//@formatter:off
			String target, key; long min, val;
			while (shops.size() > queue) {
				target = null; min = System.currentTimeMillis();
				for (Entry<String, CateHolder> e : shops.entrySet()) {
					if (b64.equals(key = e.getKey())) continue;
					if ((val = e.getValue().access) < min) {
						target = key; min = val;
					}
				}
				if (target != null) shops.remove(target);
			}
			//@formatter:on
		}
		shopx.unlock();
		return holder;
	}

	private ArrayList<Cate> roots;
	private HashMap<String, Cate> reference, mapping; // protected for Cate use

	private CateHolder() {
		super();
	}

	public ArrayList<Cate> roots() {
		access();
		return roots;
	}

	public Cate ref(String uid) {
		access();
		if (uid == null) return null;
		return reference.get(uid);
	}

	public Cate find(String display) {
		access();
		if (display == null) return null;
		return mapping.get(display);
	}

	private transient long access;

	public long access() {
		return access = System.currentTimeMillis();
	}

	private long lastC = -1L, lastI = -1L;

	private void copy(CateHolder holder) {
		roots = holder.roots;
		reference = holder.reference;
		mapping = holder.mapping;
		lastC = holder.lastC;
		lastI = holder.lastI;
	}

	private transient Lock x = new Lock();

	private static class Lock {
		private boolean lock;

		private void lock() {
			synchronized (this) {
				while (lock) {
					try {
						this.wait();
					} catch (InterruptedException e) {}
				}
			}
			lock = true;
		}

		private void unlock() {
			lock = false;
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	private static final <T extends Serializable> T write(File file, T object) {
		try (FileOutputStream out = new FileOutputStream(file)) {
			SerializationUtils.serialize(object, out);
		} catch (Throwable e) {}
		return object;
	}

	@SuppressWarnings("unchecked")
	private static final <T> T read(File file) {
		try (FileInputStream in = new FileInputStream(file)) {
			return (T) SerializationUtils.deserialize(in);
		} catch (Throwable e) {}
		return null;
	}
}