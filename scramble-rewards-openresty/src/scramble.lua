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
local cjson = require("cjson")

-- create redis client instance
local red = redis:new()
red:set_timeout(1000)

-- connect
local ok, err = red:connect("192.168.100.85", 6379)
if not ok then
  ngx.say("failed to connect: ", err)
  return
end

-- scramble
ngx.req.read_body()
local data = ngx.req.get_body_data()
if data then
  local body = cjson.decode(data)
  local user = body["user"]
  
  -- 获取user是否已领取
  local userKeys, err = red:keys(user .. ":*")
  if not userKeys then
    ngx.say("failed to get userKeys: ", err)
    return
  end
  
  if #userKeys ~= 0 then
    ngx.say(user, " already got a pack.")
    return
  end
  
  -- 获取一个红包ID
  local packKey, err = red:zrange("pack", 0, 0)
  if not packKey then
    ngx.say("zrange failed", err)
    return
  end
  
  if #packKey == 0 then
    ngx.say("all packs have been scrambled out.")
    return
  end
  
  -- 检查是否已被领取
  local packKeys, err = red:keys("*:" .. packKey[1])
  if not packKeys then
    ngx.say("failed to get packKeys: ", err)
    return
  end
  
  if #packKeys ~= 0 then
    ngx.say(packKey[1], " already been consumed.")
    local zremResult1, err = red:zrem("pack", packKey[1])
    if not zremResult1 then
      ngx.say("failed to zrem1: ", err)
      return
    end
    return
  end
  
  -- 领取红包
  local zremResult, err = red:zrem("pack", packKey[1])
  if not zremResult then
    ngx.say("failed to zrem: ", err)
    return
  end
  
  if zremResult == 0 then
    ngx.say(packKey[1], " already been consumed...")
    return
  end
  
  local setPackResult, err = red:set(user .. ":" .. packKey[1], os.time())
  if not setPackResult then
    ngx.say("failed to setPackResult: ", err)
    return
  end
  
  ngx.say(user, " processed")
end

-- close or return to pool
close_redis(red)
