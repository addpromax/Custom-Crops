# 🌾 Custom-Crops 收获追踪系统 - PlaceholderAPI 集成

## 📋 功能概述

Custom-Crops 的收获追踪系统提供了完整的 PlaceholderAPI 支持，可以追踪：
- ✅ 玩家总收获次数
- ✅ 每种作物的收获次数
- ✅ 每种品质物品的获得数量
- ✅ 收获状态检查
- ✅ 统计信息

## 📊 数据追踪

### 自动追踪的数据
1. **作物收获**: 每次玩家破坏作物时自动记录
2. **品质物品**: 追踪所有通过 `quality-crop` 动作掉落的物品
3. **持久化存储**: 所有数据保存在 `plugins/CustomCrops/harvest-data/` 目录

### 数据存储格式
```json
{
  "uuid": "12345678-1234-1234-1234-123456789012",
  "harvests": {
    "strawberry": 150,
    "wheat": 200,
    "carrot": 75
  },
  "quality_items": {
    "strawberry_silver": 50,
    "strawberry_gold": 20,
    "wheat_diamond": 30
  },
  "total_harvests": 425,
  "last_updated": 1234567890123
}
```

## 🏷️ PlaceholderAPI 变量

### 基础统计

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `%customcrops_total_harvests%` | 玩家总收获次数 | `425` |
| `%customcrops_unique_crops%` | 已收获的作物种类数 | `3` |
| `%customcrops_unique_qualities%` | 已获得的品质物品种类数 | `3` |

### 作物收获统计

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `%customcrops_crop_<cropId>%` | 特定作物的收获次数 | `%customcrops_crop_strawberry%` → `150` |
| `%customcrops_has_harvested_<cropId>%` | 是否收获过该作物 | `%customcrops_has_harvested_strawberry%` → `true` |

### 品质物品统计

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `%customcrops_quality_<itemId>%` | 特定品质物品的获得数量 | `%customcrops_quality_strawberry_gold%` → `20` |
| `%customcrops_has_quality_<itemId>%` | 是否获得过该品质物品 | `%customcrops_has_quality_strawberry_gold%` → `true` |

## 💡 使用示例

### 1. 在记分板中显示

```yaml
# 使用 FeatherBoard 或其他记分板插件
scoreboard:
  lines:
    - "&6&l农场统计"
    - "&7总收获: &f%customcrops_total_harvests%"
    - "&7草莓: &f%customcrops_crop_strawberry%"
    - "&7金星草莓: &f%customcrops_quality_strawberry_gold%"
```

### 2. 在聊天中显示

```yaml
# 使用 DeluxeChat 或其他聊天插件
format: "{prefix} %player% &7[&e收获: %customcrops_total_harvests%&7] &f{message}"
```

### 3. 在GUI中使用

```yaml
# 使用 ChestCommands 或其他GUI插件
items:
  strawberry_stats:
    material: WHEAT
    name: "&e草莓统计"
    lore:
      - "&7收获次数: &f%customcrops_crop_strawberry%"
      - "&7普通: &f%customcrops_quality_strawberry%"
      - "&7银星: &f%customcrops_quality_strawberry_silver%"
      - "&7金星: &f%customcrops_quality_strawberry_gold%"
```

### 4. 在全息图中显示

```yaml
# 使用 DecentHolograms 或其他全息插件
holograms:
  farm_stats:
    lines:
      - "&6&l你的农场数据"
      - "&7总收获: &f%customcrops_total_harvests%"
      - "&7已收获作物: &f%customcrops_unique_crops% 种"
```

### 5. 条件显示

使用 PlaceholderAPI 的条件功能：

```yaml
# 只有收获过草莓才显示
lore:
  - "%customcrops_has_harvested_strawberry% == true ? &a已解锁草莓 : &7未解锁草莓"
  - "%customcrops_has_quality_strawberry_gold% == true ? &6拥有金星草莓 : &7未获得金星"
```

### 6. 成就系统集成

```yaml
# 使用 CrazyAdvancementsAPI 或其他成就插件
achievements:
  first_harvest:
    condition: "%customcrops_total_harvests% >= 1"
    reward:
      - "eco give %player% 100"
  
  strawberry_master:
    condition: "%customcrops_crop_strawberry% >= 100"
    reward:
      - "give %player% diamond 5"
  
  gold_collector:
    condition: "%customcrops_quality_strawberry_gold% >= 10"
    reward:
      - "give %player% emerald 10"
```

## 🎯 实际应用场景

### 图鉴系统

```yaml
encyclopedia_item:
  material: WHEAT
  name: "&e%crop_name%"
  lore:
    - "%customcrops_has_harvested_%crop_id% == true ? &a✓ 已解锁 : &7✗ 未解锁"
    - "%customcrops_has_harvested_%crop_id% == true ? &7收获次数: &f%customcrops_crop_%crop_id% : &8收获一次以解锁"
    - ""
    - "%customcrops_has_quality_%quality_1% == true ? &7普通: &f%customcrops_quality_%quality_1% : &8未获得"
    - "%customcrops_has_quality_%quality_2% == true ? &7银星: &f%customcrops_quality_%quality_2% : &8未获得"
    - "%customcrops_has_quality_%quality_3% == true ? &6金星: &f%customcrops_quality_%quality_3% : &8未获得"
```

### 排行榜

```yaml
# 使用 ajLeaderboards 或其他排行榜插件
leaderboards:
  total_harvests:
    placeholder: "%customcrops_total_harvests%"
    title: "&6&l总收获排行榜"
    format: "&e#{rank}. &f{player} &7- &e{value} 次"
  
  strawberry_harvests:
    placeholder: "%customcrops_crop_strawberry%"
    title: "&c&l草莓收获排行榜"
    format: "&e#{rank}. &f{player} &7- &c{value} 个"
```

### 称号系统

```yaml
# 使用 UltraCosmetics 或其他称号插件
titles:
  novice_farmer:
    display: "&7[&a初级农夫&7]"
    requirement: "%customcrops_total_harvests% >= 100"
  
  master_farmer:
    display: "&7[&6农场大师&7]"
    requirement: "%customcrops_total_harvests% >= 1000"
  
  strawberry_king:
    display: "&7[&c草莓之王&7]"
    requirement: "%customcrops_crop_strawberry% >= 500"
  
  gold_hunter:
    display: "&7[&6金星猎人&7]"
    requirement: "%customcrops_quality_strawberry_gold% >= 50"
```

## 🔧 配置作物ID和物品ID

### 获取作物ID
作物ID在 Custom-Crops 的配置文件中定义：
```yaml
# contents/crops/default.yml
crops:
  strawberry:  # <- 这是作物ID
    seed: "strawberry_seed"
    # ...
```

使用: `%customcrops_crop_strawberry%`

### 获取品质物品ID
品质物品ID在作物的 break-actions 中定义：
```yaml
crops:
  strawberry:
    break-actions:
      - type: quality-crop
        min: 1
        max: 3
        items:
          1: "strawberry"         # <- 普通品质ID
          2: "strawberry_silver"  # <- 银星品质ID
          3: "strawberry_gold"    # <- 金星品质ID
```

使用: 
- `%customcrops_quality_strawberry%`
- `%customcrops_quality_strawberry_silver%`
- `%customcrops_quality_strawberry_gold%`

## 📱 API 调用

对于插件开发者，也可以直接使用 Java API：

```java
import net.momirealms.customcrops.api.BukkitCustomCropsPlugin;
import net.momirealms.customcrops.api.data.HarvestDataManager;

HarvestDataManager manager = BukkitCustomCropsPlugin.getInstance()
    .getHarvestDataManager();

// 获取数据
UUID playerId = player.getUniqueId();
int totalHarvests = manager.getTotalHarvests(playerId);
int strawberryCount = manager.getHarvestCount(playerId, "strawberry");
int goldStrawberry = manager.getQualityItemCount(playerId, "strawberry_gold");

// 检查状态
boolean hasHarvested = manager.hasHarvested(playerId, "strawberry");
boolean hasGold = manager.hasObtainedQualityItem(playerId, "strawberry_gold");
```

## 🐛 故障排除

### 占位符显示为空或不更新

1. **检查 PlaceholderAPI 是否已安装**
   ```
   /papi info customcrops
   ```

2. **检查占位符格式是否正确**
   - 作物ID必须与配置文件中的ID完全匹配
   - 品质物品ID必须与配置中的ID完全匹配

3. **测试占位符**
   ```
   /papi parse me %customcrops_total_harvests%
   ```

4. **检查数据是否存在**
   - 玩家必须至少收获过一次作物
   - 数据文件位于: `plugins/CustomCrops/harvest-data/<uuid>.json`

### 品质物品不记录

1. **确认使用了 quality-crop 动作**
   - 只有通过 `quality-crop` 动作掉落的物品才会被追踪

2. **检查物品ID配置**
   - 确保物品ID在物品插件（如 ItemsAdder, Oraxen）中存在

3. **查看调试日志**
   ```yaml
   # config.yml
   debug: true
   ```

## 📊 数据统计最佳实践

1. **使用有意义的作物ID**: 如 `strawberry` 而不是 `crop_001`
2. **品质等级命名规范**: 建议使用 `<crop>_<quality>` 格式
3. **定期备份数据**: 数据文件在 `harvest-data/` 文件夹中
4. **监控数据大小**: 大型服务器建议定期清理旧数据

## 🎨 显示格式建议

### 颜色编码
- 普通品质: `&f` (白色)
- 银星品质: `&7` (灰色) 或 `&b` (青色)
- 金星品质: `&6` (金色) 或 `&e` (黄色)
- 钻石品质: `&b` (青色) 或 `&d` (紫色)

### 数字格式化
使用 PlaceholderAPI 的数字格式化：
```
%math_0_<placeholder>%  # 整数
%math_1_<placeholder>%  # 一位小数
```

## 📞 技术支持

- **Wiki**: 查看 Custom-Crops 官方 Wiki
- **Discord**: 加入官方 Discord 服务器
- **GitHub**: 提交 Issue 或 Pull Request

---

**版本**: 1.0  
**兼容性**: 
- Custom-Crops 3.6+
- PlaceholderAPI 2.11+
- Bukkit/Spigot/Paper 1.16+

