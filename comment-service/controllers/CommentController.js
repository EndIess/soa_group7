/**
 * Created by Jiravat on 3/31/2017.
 */
const CommentService = require('../services/CommentService');

const models = require('../models');
const Comment = models.Comment;


module.exports = {
    createSingle,
    updateSingle,
    getSingle,
    deleteSingle,
    getCommentsByPostId,
    deleteCommentsByPostId,
    countCommentByPostId
};

function* createSingle(req, res) {
    req.body.userId = req.query.userId;
    req.body.postId = req.params.postId;
    const comment = yield CommentService.create(req.body);
    if(comment.error)res.json({"msg": "not create"}, 304);
    else{ res.status(201).json(comment);}
}

function* updateSingle(req, res) {
    const comment = yield CommentService.update(req.params.commentId, req.body);
    if (comment.error) res.json({"msg": "not found"}, 404);
    else res.status(200).json(comment);
}

function* deleteSingle(req, res) {
    const comment = yield CommentService.deleteSingle(req.params.commentId);
    if (comment.error) res.json({"msg": "not found"}, 404);
    else res.status(200).json({"msg": "delete success", comment});
}

function* getSingle(req, res) {
    const comment = yield CommentService.getSingle(req.params.commentId);
    if (comment.error) res.json({"msg": "not found"}, 404);
    else res.status(200).json(comment);
}

function* getCommentsByPostId(req, res) {
    const postId = req.params.postId;
    const limit = req.query.limit || 10;
    const page = req.query.page || 0;
    try{
        Comment.find({'postId': postId})
            .skip(page*limit)
            .limit(limit)
            .exec(function(err,comments) {
                if (err) {

                return res.json({"msg": "error"}, 404);
            }
            comments = {
                "postId": postId,
                "comments" :comments
            };
            res.status(200).json(comments);
        });
    }
    catch(e){
        return res.json({"msg": "error"}, 404);
    }

}

function* deleteCommentsByPostId(req, res) {
    const postId = req.params.postId;
    try{
        Comment.remove({'postId': postId})
            .exec(function(err) {
            if (!err) {
                res.status(200).json({"msg": "Removed"});
            }
            else {
                res.status(404).json({"msg": "error"});
            }
        });
    }
    catch(e){
        return res.status(404).json({"msg": "error"});
    }

}

function* countCommentByPostId(req, res) {
    const postId = req.params.postId;
    try{
        Comment.find({'postId': postId})
            .exec(function(err, comments) {

                if (!err) {
                    res.status(200).json({"count": comments.length});
                }
                else {
                    res.status(404).json({"msg": "error"});
                }
            });
    }
    catch(e){
        return res.status(404).json({"msg": "error"});
    }

}
