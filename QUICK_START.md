# ⚡ Custom-Crops 收获追踪系统 - 快速开始

## 🚀 功能已完成

✅ **已删除**: 命令系统（HarvestStatsCommand.java）  
✅ **已添加**: 完整的 PlaceholderAPI 支持  
✅ **已添加**: 作物收获次数追踪  
✅ **已添加**: 品质物品获得数量追踪  
✅ **已添加**: 自动持久化存储（JSON）  

## 📦 包含的文件

### API 模块
1. **PlayerHarvestData.java** - 玩家数据模型（支持作物和品质物品）
2. **HarvestDataManager.java** - 数据管理器

### 插件模块
3. **HarvestTracker.java** - 事件监听器（追踪收获和品质掉落）
4. **HarvestPlaceholderExpansion.java** - PlaceholderAPI 扩展

### 修改的文件
5. **BukkitCustomCropsPlugin.java** - 添加 HarvestDataManager 引用
6. **BukkitCustomCropsPluginImpl.java** - 初始化和注册

## 🎯 PlaceholderAPI 变量速查

### 基础统计
```
%customcrops_total_harvests%      # 总收获次数
%customcrops_unique_crops%        # 已收获作物种类数
%customcrops_unique_qualities%    # 已获得品质物品种类数
```

### 作物统计
```
%customcrops_crop_strawberry%           # 草莓收获次数
%customcrops_has_harvested_strawberry%  # 是否收获过草莓 (true/false)
```

### 品质物品统计
```
%customcrops_quality_strawberry_gold%       # 金星草莓获得数量
%customcrops_has_quality_strawberry_gold%   # 是否获得过金星草莓 (true/false)
```

## 💡 快速示例

### 1. 在 GUI 中显示（ChestCommands）

```yaml
strawberry_info:
  material: WHEAT_SEEDS
  name: "&c草莓"
  lore:
    - "&7收获次数: &f%customcrops_crop_strawberry%"
    - "&7普通草莓: &f%customcrops_quality_strawberry%"
    - "&7银星草莓: &7%customcrops_quality_strawberry_silver%"
    - "&6金星草莓: &e%customcrops_quality_strawberry_gold%"
```

### 2. 在记分板中显示（FeatherBoard）

```yaml
scoreboard:
  lines:
    - "&6&l=== 农场统计 ==="
    - "&7总收获: &f%customcrops_total_harvests%"
    - "&7草莓: &c%customcrops_crop_strawberry%"
    - "&7金星草莓: &6%customcrops_quality_strawberry_gold%"
```

### 3. 条件显示

```yaml
# 只有收获过才显示详细信息
lore:
  - "%customcrops_has_harvested_strawberry% == true ? &a已解锁 : &7未解锁"
```

## 📊 数据存储

**位置**: `plugins/CustomCrops/harvest-data/<player-uuid>.json`

**格式示例**:
```json
{
  "uuid": "12345678-1234-1234-1234-123456789012",
  "harvests": {
    "strawberry": 150,
    "wheat": 200
  },
  "quality_items": {
    "strawberry_silver": 50,
    "strawberry_gold": 20
  },
  "total_harvests": 350,
  "last_updated": 1234567890123
}
```

## 🔧 如何获取作物ID和物品ID

### 作物ID
在 `contents/crops/*.yml` 中查找：
```yaml
crops:
  strawberry:  # <- 这就是作物ID
    seed: "strawberry_seed"
```

### 品质物品ID
在作物的 break-actions 中查找：
```yaml
break-actions:
  - type: quality-crop
    items:
      1: "strawberry"         # 普通品质
      2: "strawberry_silver"  # 银星品质
      3: "strawberry_gold"    # 金星品质
```

## 🧪 测试命令

```bash
# 测试 PlaceholderAPI 是否正常工作
/papi info customcrops

# 测试具体占位符
/papi parse me %customcrops_total_harvests%
/papi parse me %customcrops_unique_crops%
/papi parse me %customcrops_crop_strawberry%
/papi parse me %customcrops_quality_strawberry_gold%
/papi parse me %customcrops_has_harvested_strawberry%
```

## 🐛 故障排除

### 占位符不工作？

1. **确认 PlaceholderAPI 已安装**
   ```
   /papi info customcrops
   ```

2. **确认作物ID正确**
   - 检查配置文件中的作物ID
   - ID区分大小写

3. **确认玩家有数据**
   - 玩家至少需要收获过一次作物
   - 检查 `harvest-data/<uuid>.json` 是否存在

### 品质物品不记录？

1. **必须使用 quality-crop 动作**
   - 普通的 drop-item 不会被追踪
   - 只有 quality-crop 类型的掉落才会记录

2. **启用调试模式**
   ```yaml
   # config.yml
   debug: true
   ```

## 📈 Java API 使用

```java
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.data.HarvestDataManager;

// 获取管理器
HarvestDataManager manager = BukkitCustomCropsPlugin.getInstance()
    .getHarvestDataManager();

UUID playerId = player.getUniqueId();

// 查询数据
int total = manager.getTotalHarvests(playerId);
int strawberries = manager.getHarvestCount(playerId, "strawberry");
int goldStrawberries = manager.getQualityItemCount(playerId, "strawberry_gold");

// 检查状态
boolean hasHarvested = manager.hasHarvested(playerId, "strawberry");
boolean hasGold = manager.hasObtainedQualityItem(playerId, "strawberry_gold");

// 手动添加（通常不需要）
manager.addHarvest(playerId, "strawberry", 1);
manager.addQualityItem(playerId, "strawberry_gold", 1);
```

## 📚 完整文档

详细文档请查看: **PAPI_HARVEST_TRACKING.md**

## ✨ 特性总结

- ✅ 自动追踪所有作物收获
- ✅ 自动追踪所有品质物品掉落
- ✅ 完整的 PlaceholderAPI 支持
- ✅ 自动保存和加载（JSON格式）
- ✅ 线程安全的数据管理
- ✅ 支持图鉴、成就、排行榜等系统集成
- ✅ 玩家进入/退出自动处理
- ✅ 插件重载支持

---

**版本**: 1.0  
**完成日期**: 2025-10-02  
**状态**: ✅ 完成并可用

