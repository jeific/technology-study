    使用模态窗口，您需要有某种触发器。您可以使用按钮或链接。这里我们使用的是按钮。
    如果您仔细查看上面的代码，您会发现在 <button> 标签中，data-target="#myModal" 是您想要在页面上加载的模态框的目标。您可以在页面上创建多个模态框，然后为每个模态框创建不同的触发器。现在，很明显，您不能在同一时间加载多个模块，但您可以在页面上创建多个在不同时间进行加载。
    在模态框中需要注意两点：
        第一是 .modal，用来把 <div> 的内容识别为模态框。
        第二是 .fade class。当模态框被切换时，它会引起内容淡入淡出。
    aria-labelledby="myModalLabel"，该属性引用模态框的标题。
    属性 aria-hidden="true" 用于保持模态窗口不可见，直到触发器被触发为止（比如点击在相关的按钮上）。
    <div class="modal-header">，modal-header 是为模态窗口的头部定义样式的类。
    class="close"，close 是一个 CSS class，用于为模态窗口的关闭按钮设置样式。
    data-dismiss="modal"，是一个自定义的 HTML5 data 属性。在这里它被用于关闭模态窗口。
    class="modal-body"，是 Bootstrap CSS 的一个 CSS class，用于为模态窗口的主体设置样式。
    class="modal-footer"，是 Bootstrap CSS 的一个 CSS class，用于为模态窗口的底部设置样式。
    data-toggle="modal"，HTML5 自定义的 data 属性 data-toggle 用于打开模态窗口。