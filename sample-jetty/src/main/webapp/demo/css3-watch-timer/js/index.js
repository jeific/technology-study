'use strict';

//https://www.uplabs.com/posts/timer-animation

var getEl = function getEl(el) {
  return document.getElementById(el);
};
var setHTML = function setHTML(el, html) {
  return el.innerHTML = html;
};
var setHTMLEl = function setHTMLEl(el, html) {
  removeLastChild(el);
  el.appendChild(html);
};
var addClass = function addClass(el, className) {
  return el.classList.add(className);
};
var removeClass = function removeClass(el, className) {
  return el.classList.remove(className);
};
var hasClass = function hasClass(el, className) {
  return el.classList.contains(className);
};
var setStyle = function setStyle(el, prop, val) {
  return el.style[prop] = val;
};
var removeLastChild = function removeLastChild(el) {
  if (el.hasChildNodes()) el.removeChild(el.lastChild);
};

var WRAPPER = getEl('wrapper'),
    TIMER = getEl('timer'),
    TIME = getEl('time'),
    SEC = getEl('sec'),
    CENTI_SEC = getEl('centi-sec'),
    BASE = getEl('base'),
    DIAL_TRACK = getEl('dial-track'),
    DIAL_DOTS_CONTAINER = getEl('dial-dots'),
    DIAL_DOTS = document.getElementsByClassName('dot-wrapper'),
    DIAL = getEl('dial'),
    DIAL_BALL = getEl('dial-ball'),
    CENTER_BAND = getEl('center-band'),
    CENTER = getEl('center'),
    START_BUTTON = getEl('start-button'),
    GREEN_OPTION = getEl('green'),
    BLUE_OPTION = getEl('blue'),
    YELLOW_OPTION = getEl('yellow'),
    RED_OPTION = getEl('red');

var STATE = {
  theme: 'green',
  increments: 61,
  timer: {
    running: false,
    centis: 0
  },
  dial: {
    isMouseDown: false,
    index: 0
  }
};

var formatTime = function formatTime(time) {
  return time < 10 ? '0' + time : time;
};

var appendDialDots = function appendDialDots() {
  var d = DIAL_DOTS_CONTAINER.clientWidth,
      r = d / 2,
      cX = r,
      cY = r,
      increment = 360 / STATE.increments;
  var angle = 0,
      rad = 0,
      i = 0,
      x = 0,
      y = 0;

  for (i = 0; i < STATE.increments; i++) {
    var dotWrapper = document.createElement('div'),
        dot = document.createElement('div');

    addClass(dotWrapper, 'dot-wrapper');
    addClass(dot, 'dot');
    dotWrapper.appendChild(dot);

    angle = increment * i - 90;
    rad = angle * (Math.PI / 180);
    x = (cX + r * Math.cos(rad)).toFixed(2);
    y = (cY + r * Math.sin(rad)).toFixed(2);

    setStyle(dotWrapper, 'left', x + 'px');
    setStyle(dotWrapper, 'top', y + 'px');

    dotWrapper.setAttribute('data-x', x);
    dotWrapper.setAttribute('data-y', y);
    dotWrapper.setAttribute('data-index', i);
    DIAL_DOTS_CONTAINER.appendChild(dotWrapper);
  }
};

var getClosestDot = function getClosestDot(mouseX, mouseY) {
  var i = 0,
      diffX = 1000,
      diffY = 1000,
      diffTot = diffX + diffY,
      currDiffTot = diffTot,
      targetDot = null;
  for (i = 0; i < DIAL_DOTS.length; i++) {
    diffX = Math.abs(mouseX - DIAL_DOTS[i].dataset.x);
    diffY = Math.abs(mouseY - DIAL_DOTS[i].dataset.y);
    currDiffTot = diffX + diffY;
    if (currDiffTot < diffTot) {
      diffTot = currDiffTot;
      targetDot = DIAL_DOTS[i];
      STATE.dial.index = i;
    }
  }
  return targetDot;
};

var expandDots = function expandDots(index) {
  var i = 0;
  for (i = 0; i < index; i++) {
    addClass(DIAL_DOTS[i], 'expanded');
  }
  for (i = index; i < STATE.increments; i++) {
    removeClass(DIAL_DOTS[i], 'expanded');
  }
};

var onCountDown = function onCountDown() {
  if (STATE.timer.centis == 0) {
    STATE.timer.centis = 100;
    if (STATE.dial.index > 0) {
      STATE.dial.index--;
      var nextDot = DIAL_DOTS[STATE.dial.index];
      setStyle(DIAL, 'left', nextDot.dataset.x + 'px');
      setStyle(DIAL, 'top', nextDot.dataset.y + 'px');
      setHTML(SEC, formatTime(STATE.dial.index));
      expandDots(STATE.dial.index);
    } else {
      clearInterval(countDownInterval);
      removeClass(START_BUTTON, 'show-pause');
      STATE.timer.running = false;
    }
  } else {
    STATE.timer.centis--;
    setHTML(CENTI_SEC, formatTime(STATE.timer.centis));
  }
};

var countDownInterval = null;
var runCountDown = function runCountDown() {
  STATE.timer.running = true;
  addClass(START_BUTTON, 'show-pause');
  countDownInterval = setInterval(onCountDown, 10);
};

var pauseCountDown = function pauseCountDown() {
  STATE.timer.running = false;
  clearInterval(countDownInterval);
  removeClass(START_BUTTON, 'show-pause');
};

var initiateSettingTimer = function initiateSettingTimer() {
  STATE.dial.isMouseDown = true;
  addClass(TIMER, 'setting');
};

var settingTimer = function settingTimer(e) {
  if (STATE.dial.isMouseDown) {
    var top = DIAL_TRACK.getBoundingClientRect().top,
        left = DIAL_TRACK.getBoundingClientRect().left,
        mouseX = e.clientX - left,
        mouseY = e.clientY - top;
    var targetDot = getClosestDot(mouseX, mouseY);
    expandDots(targetDot.dataset.index);
    setStyle(DIAL, 'left', targetDot.dataset.x + 'px');
    setStyle(DIAL, 'top', targetDot.dataset.y + 'px');
    setHTML(SEC, formatTime(targetDot.dataset.index));
  }
};

var endSettingTimer = function endSettingTimer() {
  STATE.dial.isMouseDown = false;
  removeClass(TIMER, 'setting');
};

var resetCentis = function resetCentis() {
  STATE.timer.centis = 0;
  setHTML(CENTI_SEC, formatTime(0));
};

var onStartButtonClick = function onStartButtonClick() {
  if (STATE.dial.index == 0) return;
  if (!STATE.timer.running) {
    runCountDown();
  } else {
    pauseCountDown();
  }
};

var clearSelectedTheme = function clearSelectedTheme() {
  removeClass(GREEN_OPTION, 'selected');
  removeClass(BLUE_OPTION, 'selected');
  removeClass(YELLOW_OPTION, 'selected');
  removeClass(RED_OPTION, 'selected');
};

var changeTheme = function changeTheme(color) {
  removeClass(WRAPPER, 'theme-' + STATE.theme);
  addClass(WRAPPER, 'theme-' + color);
  clearSelectedTheme();
  var option = null;
  switch (color) {
    case 'green':
      option = GREEN_OPTION;
      break;
    case 'blue':
      option = BLUE_OPTION;
      break;
    case 'yellow':
      option = YELLOW_OPTION;
      break;
    case 'red':
      option = RED_OPTION;
      break;
  }
  addClass(option, 'selected');
  STATE.theme = color;
};

DIAL_TRACK.onmousemove = function (e) {
  settingTimer(e);
};

DIAL_BALL.onmousedown = function () {
  initiateSettingTimer();
  pauseCountDown();
  resetCentis();
};

DIAL_BALL.onmouseup = function () {
  endSettingTimer();
};

DIAL_BALL.onmouseleave = function () {
  endSettingTimer();
};

START_BUTTON.onclick = function () {
  onStartButtonClick();
};

GREEN_OPTION.onclick = function () {
  changeTheme('green');
};

YELLOW_OPTION.onclick = function () {
  changeTheme('yellow');
};

BLUE_OPTION.onclick = function () {
  changeTheme('blue');
};

RED_OPTION.onclick = function () {
  changeTheme('red');
};

window.onload = function () {
  appendDialDots();
  setTimeout(function () {
    removeClass(TIMER, 'animate-in');
    addClass(SEC, 'entrance');
    addClass(CENTI_SEC, 'entrance');
  }, 2500);
};