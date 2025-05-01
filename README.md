# 文件搜索工具代码解析

这个Java Swing应用程序是一个文件搜索工具，允许用户在指定目录中搜索包含特定关键词的文件。下面我将详细解释每个方法的实现逻辑和调用的函数：

## 1. 构造函数和初始化方法

### `Search()` 构造函数

- 调用`initializeUI()`方法初始化用户界面  
- 这是程序的入口点，创建主窗口  

### `initializeUI()` 方法

- 设置窗口标题、大小、关闭操作和位置  
- 调用`initTextStyles()`初始化文本样式  
- 创建顶部面板包含目录输入框、浏览按钮、搜索框和搜索按钮  
- 创建结果显示区域(JTextPane)和滚动面板  
- 创建底部打开文件按钮  
- 设置布局管理器(BorderLayout)  
- 添加鼠标监听器处理单击和双击事件  

## 2. 文本样式相关方法

### `initTextStyles()` 方法

- 创建`StyleContext`对象管理文本样式  
- 创建默认文本样式(黑色宋体14号)  
- 创建高亮文本样式(黄色背景)  
- 使用`StyleConstants`类设置样式属性  

## 3. 目录选择方法

### `browseDirectory()` 方法

- 创建`JFileChooser`文件选择器  
- 设置只允许选择目录  
- 设置初始目录为当前输入框中的目录  
- 显示对话框并获取用户选择的目录  
- 更新目录输入框内容  

## 4. 搜索功能相关方法

### `performSearch()` 方法

- 获取目录和文件名输入  
- 验证输入是否为空  
- 清空结果区域，显示"正在搜索..."  
- 重置搜索状态变量  
- 禁用搜索按钮  
- 启动新线程执行实际搜索  

### `searchFile(File dir, String fileName)` 方法

- 递归搜索文件的核心方法  
- 检查目录是否存在和是否为目录  
- 列出目录下所有文件和子目录  
- 对每个文件检查是否包含搜索关键词  
- 匹配的文件添加到`foundFiles`列表  
- 在Swing线程中更新UI显示结果  
- 对子目录递归调用自身  

## 5. 结果显示相关方法

### `highlightText(String text, String keyword)` 方法

- 在结果区域插入带高亮的文本  
- 使用`Document`接口操作文本内容  
- 查找关键词位置并应用高亮样式  
- 使用`insertString()`方法插入文本  

### `appendText(String text)` 方法

- 简单地在结果区域末尾追加文本  
- 使用默认样式  

## 6. 选择结果相关方法

### `selectResultLine(Point point)` 方法

- 将鼠标点击位置转换为文档位置  
- 计算点击的行号  
- 调整行号排除状态行  
- 设置选中状态和高亮显示  
- 启用/禁用打开按钮  

## 7. 文件操作相关方法

### `openSelectedFile()` 方法

- 检查是否有选中的有效文件  
- 使用`Desktop`类尝试打开文件  
- 处理各种错误情况  
- 显示相应的提示信息  

## 8. 主方法

### `main(String[] args)` 方法

- 设置系统外观  
- 创建并显示主窗口  
- 使用`SwingUtilities.invokeLater()`确保线程安全  

## 关键类和函数调用

1. **Swing组件**:
   
   - `JFrame` - 主窗口  
   - `JTextField` - 文本输入框  
   - `JTextPane` - 富文本显示区域  
   - `JButton` - 按钮  
   - `JScrollPane` - 滚动面板  

2. **文档处理**:
   
   - `Document` - 文本内容模型  
   - `StyleContext` - 样式管理  
   - `StyleConstants` - 样式属性设置  

3. **文件操作**:
   
   - `File` - 文件和目录操作  
   - `Desktop` - 打开文件  

4. **并发处理**:
   
   - `SwingUtilities.invokeLater()` - 在EDT线程执行UI更新  
   - 使用`Thread`执行耗时搜索操作  


