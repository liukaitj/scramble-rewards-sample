local function close_redis(red)
  if not red then
    return
  end
  
  local pool_max_idle_time = 10000
  local pool_size = 100
  local ok, err = red:set_keepalive(pool_max_idle_time, pool_size)
  if not ok then
    ngx.say("set keepalive error : ", err)
  end
end

local redis = require("resty.redis")
local uuid = require("uuid")
local string = require("string")

-- create redis client instance
local red = redis:new()
red:set_timeout(1000)

-- connect
local ok, err = red:connect("192.168.100.85", 6379)
if not ok then
  ngx.say("failed to connect: ", err)
  return
end

-- set
for i = 1, 200, 1
do
  local packId = string.gsub(uuid(), "-", "")
  ok, err = red:zadd("pack", i, packId)
  if not ok then
    ngx.say("failed to zadd: ", err)
    return
  end
  ngx.say("zadd ok:", i)
end

ngx.say("zadd ok")

close_redis(red)
