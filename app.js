const express = require('express');
// const path = require('path');
const axios = require('axios');
const app = express();
var cors = require('cors')
app.use(cors())
const port = process.env.PORT || 4600;
app.listen(port, (req, res)=>{
    console.log(`running on port ${port}`);
});

var api_token = 'c8618kqad3i9fvjhsjng';

//for cors policy
app.use(function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*"); // update to match the domain you will make the request from
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    next();
});

app.get('/autoC', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/search?q=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        var raw_data = posts.data.result;
        for (let i = 0; i < raw_data.length; i++) {
            if (raw_data[i].type === 'Common Stock' && !raw_data[i].symbol.includes('.')) {
                var stock = new Object();
                stock.symbol = raw_data[i].symbol;
                stock.description = ' | ' + raw_data[i].description;
                final_result.push(stock);
            }
          }
        res.send(final_result)
    })
    .catch(error =>{
        res.status(500).send(error);
    })
});

app.get('/posts', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/profile2?symbol=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result.push(posts.data)
        res.send(final_result)
        // res.status(200).json(posts.data);
    })
    .catch(error =>{
        res.status(500).send(error);
    })

});

app.get('/latestPrice', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/quote?symbol=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result.push(posts.data)
        res.send(final_result)
        // res.status(200).json(posts.data);
    })
    .catch(error =>{
        res.status(500).send(error);
    })
});

app.get('/peer', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/peers?symbol=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result.push(posts.data)
        res.send(final_result[0])
        // res.status(200).json(posts.data);
    })
    .catch(error =>{
        res.status(500).send(error);
    })

});

app.get('/sixHourPrice', (req, res)=>{
    var ticker = req.query.ticker;
    var Totime = req.query.to
    Totime = new Date(parseInt(Totime) * 1000)
    Totime.setHours(Totime.getHours() - 7)
    var Fromtime = new Date(Totime)
    Fromtime.setHours(Fromtime.getHours() - 6)
    var basicInfoURL = "https://finnhub.io/api/v1/stock/candle?symbol=" + ticker +"&resolution=5&from=" + (Fromtime / 1000) + "&to=" + (Totime / 1000) +"&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        var raw_data = posts.data;
        var result = []
        result.push(raw_data.c);
        result.push(raw_data.t);
        for (let i = 0; i < result[1].length; i++) {
            result[1][i] = new Date(result[1][i] * 1000)
            var point = []
            point.push(Date.parse(result[1][i]))
            point.push(result[0][i])
            final_result.push(point)
        }
        res.send(final_result)
    })
    .catch(error =>{
        res.status(500).send(error);
    })
});

var to = new Date();
var cur_yyyy = to.getFullYear().toString();
var cur_mm = (to.getMonth()+1).toString();
var cur_dd  = to.getDate().toString();
var cur_formattedTime_to = cur_yyyy +'-'+("0"+(cur_mm)).slice(-2)+'-'+("0"+cur_dd).slice(-2)
var from = new Date();
from.setDate(from.getDate()-7);
var from_cur_yyyy = from.getFullYear().toString();
var from_cur_mm = (from.getMonth()+1).toString();
var from_cur_dd  = from.getDate().toString();
var cur_formattedTime_from = from_cur_yyyy +'-'+("0"+(from_cur_mm)).slice(-2)+'-'+("0"+from_cur_dd).slice(-2)
const monthNames = ["January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"];
app.get('/news', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/company-news?symbol=" + ticker +"&from=" + cur_formattedTime_from + "&to=" + cur_formattedTime_to + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        var haveimage = []
        for (let i = 0; i < posts.data.length; i++) {
            if (posts.data[i].image != '') {
                var raw_time = posts.data[i].datetime
                var date = raw_time * 1000
                // var yyyy = date.getFullYear().toString();
                // var mm = monthNames[date.getMonth()];
                // var dd  = date.getDate().toString();
                // var formattedTime = mm + " " + ("0"+dd).slice(-2) + ", " + yyyy
                posts.data[i].datetime = date
                haveimage.push(posts.data[i])
            }
        }
        if (haveimage.length < 20) {
            final_result = haveimage
        } else {
            for (let j = 0; j < 20; j++) {
                final_result.push(haveimage[j])
            }
        }
        res.send(final_result)
    })
    .catch(error =>{
        res.status(500).send(error);
    })

});

var stock_toStamp = Math.floor(Date.now() / 1000);
var stock_d = new Date(); // today!
stock_d.setFullYear(stock_d.getFullYear() - 2);
var stock_fromStamp = Math.floor(stock_d.getTime() / 1000);
app.get('/chart', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/candle?symbol=" + ticker +"&resolution=D&from=" + stock_fromStamp + "&to=" + stock_toStamp +"&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result = posts.data
        for (let i = 0; i < final_result.t.length; i++) {
            final_result.t[i] = final_result.t[i] * 1000
        }
        res.send(final_result)
    })
    .catch(error =>{
        res.status(500).send(error);
    })
});

app.get('/sentiment', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/social-sentiment?symbol=" + ticker + "&from=2022-01- 01&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result.push(posts.data)
        res.send(final_result[0])
    })
    .catch(error =>{
        res.status(500).send(error);
    })

});

app.get('/earn', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/earnings?symbol=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result = posts.data
        for (let i = 0; i < final_result.length; i++) {
            if (final_result[i].actual == null) {
                final_result[i].actual = 0;
            }
            if (final_result[i].estimate == null) {
                final_result[i].estimate = 0;
            }
            if (final_result[i].surprise == null) {
                final_result[i].surprise = 0;
            }
        }
        res.send(final_result)
        // res.status(200).json(posts.data);
    })
    .catch(error =>{
        res.status(500).send(error);
    })
});

app.get('/rec', (req, res)=>{
    var ticker = req.query.ticker;
    var basicInfoURL = "https://finnhub.io/api/v1/stock/recommendation?symbol=" + ticker + "&token="
    basicInfoURL += api_token
    var final_result = []
    axios.get(basicInfoURL).then(posts=>{
        final_result = posts.data
        res.send(final_result)
        // res.status(200).json(posts.data);
    })
    .catch(error =>{
        res.status(500).send(error);
    })

});
module.exports = app;
