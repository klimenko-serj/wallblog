import React from 'react';
import Avatar from '@material-ui/core/Avatar';
import Button from '@material-ui/core/Button';
import CssBaseline from '@material-ui/core/CssBaseline';
import TextField from '@material-ui/core/TextField';
import Link from '@material-ui/core/Link';
import Grid from '@material-ui/core/Grid';
import LockOutlinedIcon from '@material-ui/icons/LockOutlined';
import Typography from '@material-ui/core/Typography';
import { withStyles } from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import LinearProgress from '@material-ui/core/LinearProgress';
import axios from 'axios'

const styles = theme => ({
  '@global': {
    body: {
      backgroundColor: theme.palette.common.white,
    },
  },
  paper: {
    marginTop: theme.spacing(8),
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  avatar: {
    margin: theme.spacing(1),
    backgroundColor: theme.palette.secondary.main,
  },
  form: {
    width: '100%', // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
});

export default withStyles(styles)(
class SignUp extends React.Component {

    constructor(props) {
        super(props)

        this.state = {inProgress: false}
    }

    doSignUp() {

        let usrn = document.getElementById("username").value
        let pwd  = document.getElementById("password").value
        let usrFN = document.getElementById("firstName").value
        let usrLN = document.getElementById("lastName").value
        
        if(!(usrn && pwd && usrFN && usrLN)){
          alert("Fill all required fields!")
          return;
        }

        if(pwd.length < 8) {
          alert("Password is too short. (min 8 symbols)")
          return
        }

        this.setState({inProgress: true})
        
        axios.post('/api/signup', {
            username: usrn,
            password: pwd,
            firstName: usrFN,
            lastName: usrLN
          })
          .then(function (response) {
            let rs = response.status;
            if(rs == 201) {
              alert("User is created! SignIn to write and like!")
              window.location.replace("/signin");
            } else {
              this.setState({inProgress:false})
              alert("Something went wrong...")
            }
          }.bind(this))
          .catch(function (error) {
            console.log(error);
            let rs = error.response.status;
            if(rs == 403) {
              this.setState({inProgress:false});
              alert("User with this username already exisits.");
            } else {
              this.setState({inProgress:false})
              alert("Something went wrong...")
            }
          }.bind(this));
        
    }

    render() {
        const { classes } = this.props;

    return (
        <Container component="main" maxWidth="xs">
        <CssBaseline />
        <div className={classes.paper}>
            <Avatar className={classes.avatar}>
            <LockOutlinedIcon />
            </Avatar>
            <Typography component="h1" variant="h5">
            Sign up
            </Typography>
            <form className={classes.form} noValidate>
            <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                <TextField
                    autoComplete="fname"
                    name="firstName"
                    variant="outlined"
                    required
                    fullWidth
                    id="firstName"
                    label="First Name"
                    autoFocus
                />
                </Grid>
                <Grid item xs={12} sm={6}>
                <TextField
                    variant="outlined"
                    required
                    fullWidth
                    id="lastName"
                    label="Last Name"
                    name="lastName"
                    autoComplete="lname"
                />
                </Grid>
                <Grid item xs={12}>
                <TextField
                    variant="outlined"
                    required
                    fullWidth
                    id="username"
                    label="Username(login)"
                    name="username"
                    autoComplete="username"
                />
                </Grid>
                <Grid item xs={12}>
                <TextField
                    variant="outlined"
                    required
                    fullWidth
                    name="password"
                    label="Password"
                    type="password"
                    id="password"
                    autoComplete="current-password"
                />
                </Grid>
            </Grid>
            { (!this.state.inProgress) ?
            <Button
                type="button"
                fullWidth
                variant="contained"
                color="primary"
                className={classes.submit}
                onClick={this.doSignUp.bind(this)}
            >
                Sign Up
            </Button> :
            <LinearProgress />
            }
            <Grid container justify="flex-end">
                <Grid item>
                <Link href="/signin" variant="body2">
                    Already have an account? Sign in
                </Link>
                </Grid>
            </Grid>
            </form>
        </div>
        </Container>
    );
  }
})