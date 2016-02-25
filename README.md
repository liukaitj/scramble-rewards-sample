# scramble-rewards-sample
抢红包的并发测试工程，分为OpenResty、Netty、Tomcat三个版本（Tomcat版本稍后）

## 基本思路
1. 使用Redis的Sorted set存放红包信息，Sorted set中的每个元素代表一个红包，每个元素的member值为唯一的红包ID，score为从1开始递增的数字。
2. 设定一个POST的HTTP请求，POST数据为一个包含用户名的JSON对象，如 {"user":"someuser"}，代表someuser这个用户请求一个红包。
3. 解析这个HTTP请求：
  1. 如果Redis中已存有 'user:*' 这样的key，则代表该user已经抢过红包，略过后面的处理并直接返回该信息。
  2. 从Sorted set中取第一个元素，拿到该元素的member值作为红包ID。如果从Sorted set中取不到任何元素，则代表所有红包已被抢完，略过后面的处理并直接返回该信息。
  3. 判断Redis中是否已存在 '*:红包ID' 这样的key，如有，则代表该红包已在其他线程中被抢到过，并为了保险起见执行一次从Sorted set中删除该红包ID对应的元素的动作，然后略过后面的处理并直接返回该信息。
  4. 领取红包：
    1. 首先从Sorted set中删除该红包ID对应的元素，如果删除的结果为零，则代表该红包已在其他线程中被抢到过，此时略过后面的处理并直接返回该信息。
    2. 以上的检查都通过了之后，则代表该HTTP请求可以获取这个红包，向Redis中写入一条key为 'user:红包ID' 、value为当前时间戳的记录。

## 工程运行方法
### OpenResty版本
1. 参考[OpenResty官方网站](https://openresty.org/)和[Luarocks官方网站](https://luarocks.org/)，安装好OpenResty和luarocks。
2. 执行 luarocks install uuid 命令安装uuid包。 _启动Nginx后可通过在nginx.conf中设定 ngx.say("lua.path:", package.path) 和 ngx.say("lua.cpath:", package.cpath)来确定install的包在不在lua的path下。_
3. 将 scramble-rewards-openresty/src/\*.lua 拷贝至 /usr/local/openresty/lualib/app 路径下。 **注意修改这些lua脚本中Redis的配置信息**
4. 将 scramble-rewards-openresty/resources/nginx.conf 拷贝至 /usr/local/openresty/nginx/conf 路径下。
5. 执行 /usr/local/openresty/nginx/sbin/nginx ，启动Nginx。
6. 向 http://your_resty_server/set 发送请求（任意HTTP method皆可），此时会向Redis中插入一个200条红包信息的Sorted set。 **如需要修改红包的个数，修改 scramble-rewards-openresty/src/setpack.lua 脚本中的循环次数即可**
7. 用Jmeter读取 scramble-rewards-openresty/resources/scramble.jmx 脚本，按需求修改并发数并执行脚本即可观测吞吐量等数值。

### Netty版本
1. 安装好Java和Maven环境。
2. 进入 scramble-rewards-netty 路径，执行 mvn clean package 命令，并将生成的 scramble-rewards-netty-1.0-jar-with-dependencies.jar 拷贝至任意路径下。 **注意修改 scramble-rewards-netty/src/main/java/lk/lab/rewards/redis/JedisHelper.java 中的Redis配置信息**
3. 执行 java -jar /path/to/your/scramble-rewards-netty-1.0-jar-with-dependencies.jar 命令，服务将会启动在8080端口。
4. 参考上面OpenResty版本中的第6步来向Redis中写入红包信息（当然也可以采用其它方法，只要能向key为'pack'的Sorted set中写入若干条带有红包ID及对应score数值的元素即可）。
5. 用Jmeter读取 scramble-rewards-netty/src/test/resources/scramble.jmx 脚本，按需求修改并发数并执行脚本即可观测吞吐量等数值。

### Tomcat版本
_待续……_
